package grupo4.fastbuyback.Config;

import grupo4.fastbuyback.Entities.AdminUser;
import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.BartenderUser;
import grupo4.fastbuyback.Entities.Event;
import grupo4.fastbuyback.Repositories.AdminUsersRepository;
import grupo4.fastbuyback.Repositories.BarsRepository;
import grupo4.fastbuyback.Repositories.BartenderUsersRepository;
import grupo4.fastbuyback.Repositories.EventsRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds BartenderUser and AdminUser rows after startup so BCrypt hashing is available.
 * Only runs when the respective table is empty (i.e. each fresh H2 boot).
 *
 * Dev credentials — bartenders (username / password / bar):
 *   eclipse-north  / norte123     / eclipse-north
 *   eclipse-center / centro123    / eclipse-center
 *   eclipse-south  / sur123       / eclipse-south
 *   cumbia-main    / principal123 / cumbia-main
 *   cumbia-vip     / vip123       / cumbia-vip
 *
 * Dev credentials — admins (username / password / event):
 *   admin-eclipse / eclipse2025 / e1
 *   admin-cumbia  / cumbia2025  / e2
 *   admin-cosquin / cosquin2025 / e3
 *   admin-lolla   / lolla2025   / e5
 */
@Component
public class DataInitializer {

    private final BartenderUsersRepository bartenderRepo;
    private final AdminUsersRepository adminRepo;
    private final BarsRepository barsRepo;
    private final EventsRepository eventsRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(BartenderUsersRepository bartenderRepo,
                           AdminUsersRepository adminRepo,
                           BarsRepository barsRepo,
                           EventsRepository eventsRepo,
                           PasswordEncoder encoder) {
        this.bartenderRepo = bartenderRepo;
        this.adminRepo     = adminRepo;
        this.barsRepo      = barsRepo;
        this.eventsRepo    = eventsRepo;
        this.encoder       = encoder;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        seedBartenderUsers();
        seedAdminUsers();
    }

    private void seedBartenderUsers() {
        if (bartenderRepo.count() > 0) return;

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
            bartenderRepo.save(u);
        }
    }

    private void seedAdminUsers() {
        if (adminRepo.count() > 0) return;

        List<Object[]> seed = List.of(
            new Object[]{ "au1", "admin-eclipse", "eclipse2025", "e1" },
            new Object[]{ "au2", "admin-cumbia",  "cumbia2025",  "e2" },
            new Object[]{ "au3", "admin-cosquin", "cosquin2025", "e3" },
            new Object[]{ "au4", "admin-lolla",   "lolla2025",   "e5" }
        );

        for (Object[] row : seed) {
            String id       = (String) row[0];
            String username = (String) row[1];
            String password = (String) row[2];
            String eventId  = (String) row[3];

            Event event = eventsRepo.findById(eventId).orElse(null);
            if (event == null) continue;

            AdminUser u = new AdminUser();
            u.setId(id);
            u.setUsername(username);
            u.setPasswordHash(encoder.encode(password));
            u.setEvent(event);
            adminRepo.save(u);
        }
    }
}
