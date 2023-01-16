package engine

import org.hibernate.validator.constraints.Length
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.CrudRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@EnableWebSecurity
open class WebSecurityConfigurerImpl(val userDetailsService: UserDetailsService) :
    WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService).passwordEncoder(getEncoder())
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests().mvcMatchers("/actuator/shutdown").permitAll()
            .mvcMatchers("/api/register").permitAll()
            .anyRequest().authenticated()
            .and()
            .csrf().disable()
            .httpBasic()
    }

    @Bean
    open fun getEncoder() = BCryptPasswordEncoder()
}

@Entity
@Table(name = "users")
class UserItem(
    @Id
    @Email
    @NotEmpty(message = "Email cannot be empty")
    var email: String = "user",
    @Size(min = 5)
    var password: String = "password"
)

class UserDetailsImpl(user: UserItem) : UserDetails {
    private val username: String
    private val password: String
    private val rolesAndAuthorities: List<GrantedAuthority>

    init {
        username = user.email
        password = user.password
        rolesAndAuthorities = emptyList()
    }

    override fun getAuthorities() = rolesAndAuthorities

    override fun getPassword() = password

    override fun getUsername() = username

    // 4 remaining methods that just return true
    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}

@Service
class UserDetailsServiceImpl(
    val userRepo: UserRepository
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: UserItem = userRepo.findUserByEmail(username)
            ?: throw UsernameNotFoundException("Not found: $username")
        return UserDetailsImpl(user)
    }
}

@Repository
interface UserRepository : CrudRepository<UserItem, String> {
    //private val users: MutableMap<String, User> = ConcurrentHashMap()

    fun findUserByEmail(username: String): UserItem?
}

@RestController
class RegisterController(val userRepository: UserRepository, val encoder: PasswordEncoder) {
    @PostMapping("/api/register")
    fun register(@Valid @RequestBody user: UserItem): ResponseEntity<String> {
        userRepository.findUserByEmail(user.email)
            ?.let { return ResponseEntity.badRequest().build() }
        user.password = encoder.encode(user.password)
        userRepository.save(user)
        return ResponseEntity.ok(userRepository.findAll().toString())
    }
}