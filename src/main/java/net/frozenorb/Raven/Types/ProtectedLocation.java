package net.frozenorb.Raven.Types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.Location;

import javax.annotation.Nullable;

/**
 * Created by Ryan on 6/2/2015
 * <p/>
 * Project: raven
 */
@Data
@AllArgsConstructor
public class ProtectedLocation {
    @NonNull private Location parent;
    @Nullable private String password;

    public boolean isProtected() {
        return password != null && !password.isEmpty();
    }

    public boolean matches(String test) {
        return test.equalsIgnoreCase(password);
    }
}
