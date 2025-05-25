package org.bestservers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class CleanupController {

    private final UrlCleanerService cleaner;

    public CleanupController(UrlCleanerService cleaner) {
        this.cleaner = cleaner;
    }

    @PostMapping("/delete-all")
    public String deleteAll() {
        cleaner.deleteAllLinks();
        return "✅ Wszystkie wpisy zostały usunięte";
    }

    @PostMapping("/clean-expired")
    public String cleanExpired() {
        cleaner.cleanOldLinks();
        return "✅ Wpisy przeterminowane zostały usunięte";
    }
}
