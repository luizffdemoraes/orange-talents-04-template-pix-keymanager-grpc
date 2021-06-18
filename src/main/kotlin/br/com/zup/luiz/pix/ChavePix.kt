package br.com.zup.luiz.pix

import br.com.zup.luiz.TipoDeConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.*

@Entity
class ChavePix(@field:NotNull
               @Column(nullable = false)
               val clienteId: UUID,

               @field:NotNull
               @Enumerated(EnumType.STRING)
               @Column(nullable = false)
               val tipoDeChave: TipoDeChave,

               @field:NotBlank
               @Column(unique = true, nullable = false)
               var chave: String,

               @field:Valid
               @Embedded
               val conta: ContaAssociada,

               @field:NotNull
               @Enumerated(EnumType.STRING)
               @Column(nullable = false)
               val tipoDeConta: TipoDeConta
) {

    @Id
    @GeneratedValue
    val id: UUID? = null
    val criadaEm: LocalDateTime = LocalDateTime.now()
}