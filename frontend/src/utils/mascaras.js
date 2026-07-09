// Mesma regra do backend: exibir COM máscara, enviar/persistir SEM máscara.

export function somenteDigitos(valor) {
  return (valor || '').replace(/\D/g, '')
}

export function maskCpf(valor) {
  return somenteDigitos(valor)
    .slice(0, 11)
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4')
}

export function maskCep(valor) {
  return somenteDigitos(valor)
    .slice(0, 8)
    .replace(/(\d{5})(\d)/, '$1-$2')
}

// Celular: (61) 98765-4321 | Fixo: (61) 3333-4444
export function maskTelefone(tipo, valor) {
  const max = tipo === 'CELULAR' ? 11 : 10
  const digitos = somenteDigitos(valor).slice(0, max)
  if (digitos.length === 0) return ''
  if (digitos.length <= 2) return `(${digitos}`
  const corte = tipo === 'CELULAR' ? 7 : 6
  if (digitos.length <= corte) return `(${digitos.slice(0, 2)}) ${digitos.slice(2)}`
  return `(${digitos.slice(0, 2)}) ${digitos.slice(2, corte)}-${digitos.slice(corte)}`
}
