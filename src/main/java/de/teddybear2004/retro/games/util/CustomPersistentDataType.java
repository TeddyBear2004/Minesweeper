package de.teddybear2004.retro.games.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CustomPersistentDataType {

    public static final PersistentBoolean PERSISTENT_BOOLEAN = new PersistentBoolean();

    public static class PersistentBoolean implements PersistentDataType<Byte, Boolean> {

        @NotNull
        @Override
        public Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @NotNull
        @Override
        public Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @NotNull
        @Override
        public Byte toPrimitive(@NotNull Boolean complex, @NotNull PersistentDataAdapterContext context) {
            return (byte) (complex ? 1 : 0);
        }

        @NotNull
        @Override
        public Boolean fromPrimitive(@NotNull Byte primitive, @NotNull PersistentDataAdapterContext context) {
            return primitive == (byte) 1;
        }

    }

}
