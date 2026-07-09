package br.com.sea.desafio.service;

import br.com.sea.desafio.domain.Cliente;
import br.com.sea.desafio.dto.ClienteRequest;
import br.com.sea.desafio.dto.ClienteResponse;
import br.com.sea.desafio.exception.CpfDuplicadoException;
import br.com.sea.desafio.exception.RecursoNaoEncontradoException;
import br.com.sea.desafio.mapper.ClienteMapper;
import br.com.sea.desafio.repository.ClienteRepository;
import br.com.sea.desafio.util.Mascaras;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    // Logs registram apenas identificadores internos — nunca CPF, telefone ou e-mail (PII)
    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(clienteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteMapper.toResponse(obterCliente(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        String cpf = Mascaras.somenteDigitos(request.getCpf());
        if (clienteRepository.existsByCpf(cpf)) {
            throw new CpfDuplicadoException();
        }
        Cliente cliente = new Cliente();
        clienteMapper.aplicarDados(cliente, request);
        ClienteResponse response = clienteMapper.toResponse(salvar(cliente));
        log.info("Cliente criado: id={}", response.getId());
        return response;
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = obterCliente(id);
        String cpf = Mascaras.somenteDigitos(request.getCpf());
        if (clienteRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new CpfDuplicadoException();
        }
        clienteMapper.aplicarDados(cliente, request);
        ClienteResponse response = clienteMapper.toResponse(salvar(cliente));
        log.info("Cliente atualizado: id={}", id);
        return response;
    }

    @Transactional
    public void excluir(Long id) {
        clienteRepository.delete(obterCliente(id));
        log.info("Cliente excluído: id={}", id);
    }

    private Cliente obterCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: id " + id));
    }

    private Cliente salvar(Cliente cliente) {
        try {
            return clienteRepository.saveAndFlush(cliente);
        } catch (DataIntegrityViolationException e) {
            throw new CpfDuplicadoException();
        }
    }
}
