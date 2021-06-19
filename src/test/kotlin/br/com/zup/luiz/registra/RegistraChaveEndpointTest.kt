package br.com.zup.luiz.registra

import br.com.zup.luiz.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.luiz.RegistraChavePixRequest
import br.com.zup.luiz.TipoDeChave
import br.com.zup.luiz.pix.TipoDeChave.*
import br.com.zup.luiz.TipoDeConta
import br.com.zup.luiz.externo.DadosDaContaResponse
import br.com.zup.luiz.externo.InstituicaoResponse
import br.com.zup.luiz.externo.ItauClient
import br.com.zup.luiz.externo.TitularResponse
import br.com.zup.luiz.pix.ChavePix
import br.com.zup.luiz.pix.ChavePixRepository
import br.com.zup.luiz.pix.ContaAssociada
import io.grpc.Channel
import org.junit.jupiter.api.Assertions.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistraChaveEndPointTest(
    val grpcClient: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {
    @field:Inject
    lateinit var itauClient: ItauClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve cadastrar uma nova chave pix(cpf)`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("05458180011")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "000000",
            titular = TitularResponse(CLIENT_ID.toString(), "Rafael Ponte", "33333333333")
        )
        `when`(itauClient.buscaContaPorTipo(request.clienteId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        val response = grpcClient.registra(request)

        with(response) {
            assertTrue(chavePixRepository.existsByChave(request.chave))
            assertEquals(CLIENT_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

//    @Test
//    fun `deve cadastrar uma nova chave pix(celular) valida`() {
//
//    }

    @Test
    fun `deve cadastrar uma nova chave pix(email) valida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("teste@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val dadosDaContaRespose = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "000000",
            titular = TitularResponse(CLIENT_ID.toString(), "Rafael Ponte", "33333333333")
        )
        `when`(itauClient.buscaContaPorTipo(request.clienteId, request.tipoDeConta.name))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose))

        val response = grpcClient.registra(request)

        with(response) {
            assertTrue(chavePixRepository.existsByChave(request.chave))
            assertEquals(CLIENT_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

//    @Test
//    fun `deve cadastrar uma nova chave pix(aleatoria) valida`() {}

    @Test
    fun `nao deve cadastrar uma nova chave pix(cpf) invalida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("111111111111")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

//    @Test
//    fun `nao deve cadastrar uma nova chave pix(celular) invalida`() {}

    @Test
    fun `nao deve cadastrar uma nova chave pix(email) invalida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("teste.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }



    @Test
    fun `nao deve cadastrar chave pix quando ja existe`() {
        val chaveSalva = chavePixRepository.save(
            ChavePix(
                clienteId = CLIENT_ID,
                tipoDeChave = EMAIL,
                chave = "teste@email.com",
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A",
                    nomeDoTitular = "Rafael Ponte",
                    cpfDoTitular = "22222222222",
                    agencia = "0001",
                    numeroDaConta = "000000"
                )
            )
        )

        //acao
        //devemos verificar se recebemos uma runtime exception (chave ja existente)
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("teste@email.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validacao
        //aqui com nosso retorno da excecao, validamos se o status de retorno é igual ao ALREADY_EXISTS
        //alem de verificarmos se a descricao bate com a descricao recebida
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '${chaveSalva.chave}' existente", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar uma nova chave pix quando o clientId nao existir no sistema`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("teste@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENT_ID.toString(), tipo = request.tipoDeConta.name))
            .thenReturn(HttpResponse.notFound())


        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return mock(ItauClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel): KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub? {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}