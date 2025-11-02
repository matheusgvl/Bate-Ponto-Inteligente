Para rodar a aplicaçaõ de teste 

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

$env:PATH = $env:PATH + ";C:\Program Files\Java\jdk-21\bin"

.\mvnw.cmd spring-boot:run
