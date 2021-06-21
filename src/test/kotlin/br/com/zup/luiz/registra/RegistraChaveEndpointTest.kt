package br.com.zup.luiz.registra

import br.com.zup.luiz.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.luiz.RegistraChavePixRequest
import br.com.zup.luiz.TipoDeChave
import br.com.zup.luiz.TipoDeConta
import br.com.zup.luiz.integration.bcb.*
import br.com.zup.luiz.integration.itau.DadosDaContaResponse
import br.com.zup.luiz.integration.itau.InstituicaoResponse
import br.com.zup.luiz.integration.itau.ItauClient
import br.com.zup.luiz.integration.itau.TitularResponse
import br.com.zup.luiz.pix.ChavePix
import br.com.zup.luiz.pix.ChavePixRepository
import br.com.zup.luiz.pix.ContaAssociada
import br.com.zup.luiz.pix.TipoDeChave.EMAIL
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
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistraChaveEndPointTest(
    val grpcClient: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {
    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix (EMAIL)`() {
        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaRespose()))

        `when`(bcbClient.create(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // ação
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("rponte@gmail.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }


    // Vou analisar mais
//    @Test
//    internal fun `deve cadastrar uma nova chave pix Aleatoria`() {
//        val request = RegistraChavePixRequest.newBuilder()
//            .setClienteId(CLIENTE_ID.toString())
//            .setTipoDeChave(TipoDeChave.ALEATORIA)
//            .setChave("")
//            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
//            .build()
//
//        val dadosDaContaRespose = DadosDaContaResponse(
//            tipo = "CONTA_CORRENTE",
//            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A", "60701190"),
//            agencia = "0001",
//            numero = "000000",
//            titular = TitularResponse(CLIENTE_ID.toString(), "Rafael Ponte", "33333333333")
//        )
//
//        `when`(itauClient.buscaContaPorTipo(request.clienteId, request.tipoDeConta.name))
//            .thenReturn(HttpResponse.ok(dadosDaContaRespose))
//
//        val response = grpcClient.registra(request)
//
//        with(response) {
//            assertNotNull(pixId)
//            assertEquals(CLIENTE_ID.toString(), clienteId)
//        }
//    }

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

    @Test
    fun `nao deve cadastrar uma nova chave pix(celular) invalida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(TipoDeChave.CELULAR)
            .setChave("0000")
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
                clienteId = CLIENTE_ID,
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
                    .setClienteId(CLIENTE_ID.toString())
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
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("teste@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = request.tipoDeConta.name))
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

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel): KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub? {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
     fun dadosDaContaRespose(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse(id = CLIENTE_ID.toString(), "Rafael Ponte", "63657520325")
        )
    }

     fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

     fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

     fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

     fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
        )
    }

     fun chave(
        tipoDeChave: br.com.zup.luiz.pix.TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoDeChave = tipoDeChave,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}