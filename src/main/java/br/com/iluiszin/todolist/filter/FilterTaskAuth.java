package br.com.iluiszin.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.iluiszin.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {

            // Pegar a autenticação (usuário e senha)
            var authorization = request.getHeader("Authorization");
            var authEncoded = authorization.substring("basic".length()).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            var authString = new String(authDecoded);

            var username = authString.split(":")[0];
            var password = authString.split(":")[1];

            // Validar Usuário
            var userExists = this.userRepository.findByUsername(username);

            if (userExists == null) {
                response.sendError(401);
                return;
            }
            // Validar senha
            var passwordHashed = userExists.getPassword();
            var passwordValid = BCrypt.verifyer().verify(password.toCharArray(), passwordHashed).verified;

            if (!passwordValid) {
                response.sendError(401);
                return;
            }

            request.setAttribute("idUser", userExists.getId());
        }

        filterChain.doFilter(request, response);
    }

}
