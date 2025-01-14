package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.NbtSerializable;
import com.fibermc.essentialcommands.commands.exceptions.ECExceptions;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;

public class NamedLocationStorage extends HashMap<String, MinecraftLocation> implements NbtSerializable {

    public NamedLocationStorage() {
        super();
    }

    public NamedLocationStorage(NbtCompound nbt) {
        this();
        loadNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        this.forEach((key, value) -> nbt.put(key, value.asNbt()));
        return nbt;
    }

    /**
     *
     * @param nbt NbtCompound or NbtList. (Latter is deprecated)
     */
    public void loadNbt(NbtElement nbt) {
        if (nbt.getType() == 9) {
            // Legacy format
            NbtList homesNbtList = (NbtList)nbt;
            for (NbtElement t : homesNbtList) {
                NbtCompound homeTag = (NbtCompound) t;
                super.put(homeTag.getString("homeName"), new MinecraftLocation(homeTag));
            }
        } else {
            NbtCompound nbtCompound = (NbtCompound) nbt;
            nbtCompound.getKeys().forEach((key) -> super.put(key, new MinecraftLocation(nbtCompound.getCompound(key))));
        }

    }

    public MinecraftLocation putCommand(String name, MinecraftLocation location) throws CommandSyntaxException {
        if (this.get(name) == null) {
            return super.put(name, location);
        } else {
            throw ECExceptions.keyExists().create(name);
        }
    }

}
