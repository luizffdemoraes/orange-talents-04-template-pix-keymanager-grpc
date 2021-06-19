package br.com.zup.luiz.pix

import br.com.zup.luiz.TipoDeConta
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest {

    @Test
    internal fun `deve pertencer ao cliente`() {

        val clienteId = UUID.randomUUID()
        val outroClienteId = UUID.randomUUID()

        with (newChave(tipoDeChave = TipoDeChave.ALEATORIA, clienteId = clienteId)) {
            assertTrue(this.pertenceAo(clienteId))
            assertFalse(this.pertenceAo(outroClienteId))
        }
    }

    @Test
    internal fun `deve ser chave do tipo aleatoria`() {
        with (newChave(tipoDeChave = TipoDeChave.ALEATORIA)) {
            assertTrue(this.isAleatoria())
        }
    }

    @Test
    internal fun `nao deve ser chave do tipo aleatoria`() {
        with(newChave(tipoDeChave = TipoDeChave.CELULAR)) {
            assertFalse(this.isAleatoria())
        }
    }

    @Test
    internal fun `nao deve atualizar chave quando chave nao for aleatoria`() {

        val original = "asuhasiuhasdikuhds"

        with(newChave(tipoDeChave = TipoDeChave.CPF, chave = original)){
            assertFalse(atualiza("hahahahaha"))
            assertTrue(this.chave == original)
        }
    }

    @Test
    internal fun `deve atualizar quando chave for aleatoria`() {

        val original = "asuhasiuhasdikuhds"

        with(newChave(tipoDeChave = TipoDeChave.ALEATORIA, chave = original)) {
            assertTrue(atualiza("hahahahaha"))
            assertTrue(this.chave != original)
        }
    }

    private fun newChave(
        tipoDeChave: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID()
    ) : ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoDeChave = tipoDeChave,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}