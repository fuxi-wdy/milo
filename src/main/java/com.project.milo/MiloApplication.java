package com.project.milo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@MapperScan("com.project.milo.mapper")
public class MiloApplication {

    public static void main(String[] args) {
       // SpringApplication.run(MiloApplication.class, args);
        SpringApplication app = new SpringApplication(MiloApplication.class);
        app.setBanner((environment, sourceClass, out) -> {
           /* final String RESET = "\u001B[0m";
            final String CYAN  = "\u001B[96m";        // 亮青
            final String BLUE  = "\u001B[94m";        // 亮蓝
            final String MAG   = "\u001B[95m";        // 亮品红
            final String BLACK_BOLD = "\u001B[1;30m"; // 黑色加粗（名字）

            out.println();
            out.println(CYAN + "            ********            " + RESET);
            out.println(CYAN + "        ****************        " + RESET);
            out.println(BLUE + "    ************************    " + RESET);
            out.println(BLUE + "********************************" + RESET);
            out.println(MAG  + "************" + BLACK_BOLD + "四川云帆" + MAG + "************" + RESET);
            out.println(BLUE + "********************************" + RESET);
            out.println(BLUE + "    ************************    " + RESET);
            out.println(CYAN + "        ****************        " + RESET);
            out.println(CYAN + "            ********            " + RESET);
            out.println();*/

            final String RESET = "\u001B[0m";
            final String CYAN  = "\u001B[96m";   // 亮青
            final String BLUE  = "\u001B[94m";   // 亮蓝
            final String MAG   = "\u001B[95m";   // 亮品红

            out.println();
            out.println(CYAN + "███╗   ███╗  ██╗  ██╗      ██████╗ " + RESET);
            out.println(CYAN + "████╗ ████║  ██║  ██║     ██╔═══██╗" + RESET);
            out.println(BLUE + "██╔████╔██║  ██║  ██║     ██║   ██║" + RESET);
            out.println(BLUE + "██║╚██╔╝██║  ██║  ██║     ██║   ██║" + RESET);
            out.println(MAG  + "██║ ╚═╝ ██║  ██║  ███████╗╚██████╔╝" + RESET);
            out.println(MAG  + "╚═╝     ╚═╝  ╚═╝  ╚══════╝ ╚═════╝ " + RESET);
            out.println();
        });

        app.run(args);
    }

}
