package com.musiclog;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTest {

    @Test
    void verifiesApplicationModuleBoundaries() {
        ApplicationModules.of(MusicLogApplication.class).verify();
    }
}
