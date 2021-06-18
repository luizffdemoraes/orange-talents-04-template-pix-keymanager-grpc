package br.com.zup.luiz.pix

import br.com.zup.luiz.TipoDeConta
import br.com.zup.luiz.validations.ValidPixKey
import br.com.zup.luiz.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.*

@ValidPixKey
@Introspected
data class NovaChavePix(
    @ValidUUID
    @field:NotBlank val clienteId: String,
    @field:NotNull val tipoDeChave: TipoDeChave?,
    @field:NotNull val tipoDeConta: TipoDeConta?,
    @field:Size(max = 77) val chave: String
) {

    fun toModel(conta: ContaAssociada): ChavePix{
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoDeChave = TipoDeChave.valueOf(this.tipoDeChave!!.name),
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            chave = if(this.tipoDeChave == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
            conta = conta
        )
    }

}