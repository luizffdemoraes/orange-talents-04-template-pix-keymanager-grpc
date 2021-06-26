# Por favor faça um Fork desse projeto!

## Está em dúvida de como fazer um Fork? Não tem problema! [Aqui tem uma explicação do que entendemos que você deve considerar!](https://docs.github.com/en/github/getting-started-with-github/fork-a-repo)


## ===================== GRPC =====================
-> Buildar para gerar artefato
-> sh gradlew build

-> Testar aquivo
-> java -jar build/libs/key-manager-grpc-0.1-all.jar

-> Buildar o arquivo Dockerfile
-> docker build -t key-manager-grpc .

-> Listar imagem criada
-> docker image ls

-> Rodar com docker
-> docker run -d -p 50051:50051 key-manager-grpc

-> Verificar imagem
-> docker ps

-> Resolvedor dos problemas by Yuri
-> .\gradlew.bat clean build

-> Subir docker compose
-> docker-compose up -d --build

-> Verificar logs
-> docker logs -f 0877317d878a