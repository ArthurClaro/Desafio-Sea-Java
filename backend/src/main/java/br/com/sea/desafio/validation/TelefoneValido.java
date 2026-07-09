package br.com.sea.desafio.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validação em nível de classe: o número de dígitos depende do tipo do telefone
 * (celular = 11 dígitos, residencial/comercial = 10).
 */
@Documented
@Constraint(validatedBy = TelefoneValidoValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TelefoneValido {

    String message() default "Número de telefone inválido para o tipo informado";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
