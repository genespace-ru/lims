package ru.biosoft.lims;

import com.developmentontheedge.be5.jetty.EmbeddedJetty;

public class AppMain
{
    public static void main(String... args)
    {
        new EmbeddedJetty().run();
    }
}
