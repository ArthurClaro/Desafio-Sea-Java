package br.com.sea.desafio.controller;

import br.com.sea.desafio.dto.CepResponse;
import br.com.sea.desafio.service.CepService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cep")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/{cep}")
    public CepResponse consultar(@PathVariable String cep) {
        return cepService.consultar(cep);
    }
}
