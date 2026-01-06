package com.example.myapplication;

import java.util.regex.Pattern;

/**
 * Classe para validação de dados comuns em formulários de registro.
 */
public class validaDados {

    //Regex para validação de email (formato básico RFC 5322)
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";

    private static final String USER_REGEX = "^[A-Za-z0-9+_.-]{3,}$";

    private static final String EMPRESA_REGEX = "^[A-Za-zÀ-ÿ0-9&.\\s]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final Pattern USERNAME_PATTERN = Pattern.compile(USER_REGEX);
    private static final Pattern EMPRESA_PATTERN = Pattern.compile(EMPRESA_REGEX);

    /**
     * Valida um endereço de email.
     * @param email O email a ser validado.
     * @return true se o email for válido (formato correto e não vazio), false caso contrário.
     */
    public boolean validaEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida um CNPJ
     * Remove formatação e verifica dígitos, sequência e verificadores
     * @param cnpj O CNPJ a ser validado (pode incluir formatação como XX.XXX.XXX/XXXX-XX)
     * @return true se o CNPJ for válido, false caso contrário
     */
    public boolean validaCNPJ(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }

        // Remove formatação: pontos, barras, hífen, espaços
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        // Verifica se tem exatamente 14 dígitos
        if (cnpjLimpo.length() != 14) {
            return false;
        }

        // Verifica se não é uma sequência inválida (todos iguais ou zeros)
        if (cnpjLimpo.matches("0{14}") || cnpjLimpo.matches("(\\d)\\1{13}")) {
            return false;
        }

        // Calcula o primeiro dígito verificador
        int soma = 0;
        int[] pesos = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpjLimpo.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : (11 - resto);
        if (Character.getNumericValue(cnpjLimpo.charAt(12)) != digito1) {
            return false;
        }

        // Calcula o segundo dígito verificador
        soma = 0;
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpjLimpo.charAt(i)) * pesos2[i];
        }
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : (11 - resto);
        if (Character.getNumericValue(cnpjLimpo.charAt(13)) != digito2) {
            return false;
        }

        return true;
    }

    /**
     * Valida o nome da empresa (básico: não vazio e mínimo 2 caracteres).
     * @param nome O nome a ser validado.
     * @return true se válido, false caso contrário.
     */
    public boolean validaNomeEmpresa(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        return EMPRESA_PATTERN.matcher(nome.trim()).matches(); // Pode adicionar regex para evitar só números: !nomeLimpo.matches("\\d+")
    }

    /**
     * Valida a senha (básico: não vazia e mínimo 6 caracteres).
     * @param senha A senha a ser validada.
     * @return true se válida, false caso contrário.
     */
    public boolean validaSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            return false;
        }
        return senha.trim().length() >= 6;
    }
    public boolean validaUsuario(String username){
        if(username == null || username.trim().isEmpty()){
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    };
}
