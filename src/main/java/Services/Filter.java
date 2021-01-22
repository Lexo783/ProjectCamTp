package Services;

import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;

public class Filter {

    public Color getColor(String typeColor){
        switch (typeColor){
            case "Red":
                return Color.RED;
            case "Blue":
                return Color.BLUE;
            case "Green":
                return Color.GREEN;
            default:
                return null;
        }
    }

    public Lighting filterColor(Color color)
    {
        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(1.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(45, 45, color));

        return lighting;
    }

    public String getCadre(String typeCadre){
        switch (typeCadre){
            case "Classik":
                System.out.println(this.getClass().getResource("/img/cadre3c.png").toString());
                return "/img/cadre3c.png";
            case "Or":
                return "/img/cadreOr.png";
            default:
                return null;
        }
    }

    public String getCertified(String typeCadre){
        switch (typeCadre){
            case "Certifié":
                System.out.println(this.getClass().getResource("/img/certified.png").toString());
                return "/img/certified.png";
            case "Approved":
                return "/img/approved.png";
            default:
                return null;
        }
    }

}
