package ch.epfl.gameboj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;

public final class DebugMain2 {
    private static final int[] COLOR_MAP = new int[] {
      0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };
    
    private static String[] paths = new String[] {
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

//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/01-special.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/02-interrupts.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/03-op sp,hl.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/04-op r,imm.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/05-op rp.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/06-ld r,r.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/07-jr,jp,call,ret,rst.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/08-misc instrs.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/09-op r,r.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/10-bit ops.gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/11-op a,(hl).gb",
//            "C:/Users/Paul Juillard/Documents/EPFL/BA2/POO/Projet/GameBoj/test/ch/epfl/gameboj/component/blaarg/instr_timing.gb" };

    public static void main(String[] args) throws IOException {
      for(String path : paths) {
          File romFile = new File(path);
          long cycles = 30_000_000l;
    
          GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
          gb.runUntil(cycles);
    
          System.out.println("+--------------------+");
          for (int y = 0; y < 18; ++y) {
            System.out.print("|");
            for (int x = 0; x < 20; ++x) {
          char c = (char) gb.bus().read(0x9800 + 32*y + x);
          System.out.print(Character.isISOControl(c) ? " " : c);
            }
            System.out.println("|");
          }
          System.out.println("+--------------------+");
    
    //      LcdImage li = gb.lcdController().currentImage();
    //      BufferedImage i =
    //        new BufferedImage(li.width(),
    //              li.height(),
    //              BufferedImage.TYPE_INT_RGB);
    //      for (int y = 0; y < li.height(); ++y)
    //        for (int x = 0; x < li.width(); ++x)
    //      i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
    //      ImageIO.write(i, "png", new File("gb.png"));
        }
    }
  }
