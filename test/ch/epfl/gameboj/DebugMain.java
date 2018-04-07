/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class DebugMain {
    public static void main(String[] args) throws IOException {
        String[] paths = new String[] {
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/01-special.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/02-interrupts.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/03-op sp,hl.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/04-op r,imm.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/05-op rp.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/06-ld r,r.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/07-jr,jp,call,ret,rst.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/08-misc instrs.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/09-op r,r.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/10-bit ops.gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/11-op a,(hl).gb",
                "/Users/Leo/git/GameBoj/test/ch/epfl/gameboj/component/blaarg/instr_timing.gb" };

//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/01-special.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/02-interrupts.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/03-op sp,hl.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/04-op r,imm.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/05-op rp.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/06-ld r,r.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/07-jr,jp,call,ret,rst.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/08-misc instrs.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/09-op r,r.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/10-bit ops.gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/11-op a,(hl).gb",
//                "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/instr_timing.gb" };
        for (int i = 0; i < paths.length; i++) {
            File romFile = new File(paths[i]);
            long cycles = Long.parseLong(args[0]);

            GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
            Component printer = new DebugPrintComponent();
            printer.attachTo(gb.bus());
            while (gb.cycles() < cycles) {
                long nextCycles = Math.min(gb.cycles() + 17556, cycles);
                gb.runUntil(nextCycles);
                gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
            }
        }
    }
}
