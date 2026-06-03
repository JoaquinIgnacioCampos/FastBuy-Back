package grupo4.fastbuyback.Config;

import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.BartenderUser;
import grupo4.fastbuyback.Repositories.BarsRepository;
import grupo4.fastbuyback.Repositories.BartenderUsersRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds BartenderUser rows after startup so BCrypt hashing is available.
 * Only runs when the table is empty (i.e. each fresh H2 boot).
 *
 * Dev credentials (username / password):
 *   eclipse-north  / norte123
 *   eclipse-center / centro123
 *   eclipse-south  / sur123
 *   cumbia-main    / principal123
 *   cumbia-vip     / vip123
 */
@Component
public class DataInitializer {

    private final BartenderUsersRepository usersRepo;
    private final BarsRepository barsRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(BartenderUsersRepository usersRepo,
                           BarsRepository barsRepo,
                           PasswordEncoder encoder) {
        this.usersRepo = usersRepo;
        this.barsRepo  = barsRepo;
        this.encoder   = encoder;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void seedBartenderUsers() {
        if (usersRepo.count() > 0) return;

        List<Object[]> seed = List.of(
            new Object[]{ "bu1", "eclipse-north",  "norte123",      "eclipse-north"  },
            new Object[]{ "bu2", "eclipse-center", "centro123",     "eclipse-center" },
            new Object[]{ "bu3", "eclipse-south",  "sur123",        "eclipse-south"  },
            new Object[]{ "bu4", "cumbia-main",    "principal123",  "cumbia-main"    },
            new Object[]{ "bu5", "cumbia-vip",     "vip123",        "cumbia-vip"     }
        );

        for (Object[] row : seed) {
            String id       = (String) row[0];
            String username = (String) row[1];
            String password = (String) row[2];
            String barId    = (String) row[3];

            Bar bar = barsRepo.findById(barId).orElse(null);
            if (bar == null) continue;

            BartenderUser u = new BartenderUser();
            u.setId(id);
            u.setUsername(username);
            u.setPasswordHash(encoder.encode(password));
            u.setBar(bar);
            usersRepo.save(u);
        }
    }
}
