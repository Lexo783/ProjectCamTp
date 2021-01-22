package Services;

import javafx.scene.Group;
import javafx.scene.control.ChoiceBox;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    public Lighting filterColor(ChoiceBox choiceBoxFilter)
    {
        Color filterColor = setColor(choiceBoxFilter.getValue().toString());
        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(1.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(45, 45, filterColor));

        return lighting;
    }

    public String getCadre(String typeCadre){
        switch (typeCadre){
            case "Classik":
                System.out.println(this.getClass().getResource("/img/cadre3c.png").toString());
                return this.getClass().getResource("/img/cadre3c.png").toString();
            case "Or":
                return this.getClass().getResource("/img/cadreOr.png").toString();
            default:
                return null;
        }
    }

    /**
     * @deprecated
     * @param resBottom
     * @param widthBottom
     * @param heightBottom
     * @param cadreTop
     * @return
     */
    public Group filterCadre(ImageView resBottom, double widthBottom, double heightBottom, Image cadreTop)
    {
        ImageView top = new ImageView(cadreTop);

        resBottom.setFitWidth(widthBottom);
        resBottom.setFitHeight(heightBottom);

        top.setFitWidth(resBottom.getFitWidth());
        top.setFitHeight(resBottom.getFitHeight());

        top.setBlendMode(BlendMode.SRC_OVER);
        Group blend = new Group(
                resBottom,
                top
        );
        return blend;
    }

    public String getStamp(String typeStamp){
        switch (typeStamp){
            case "Approved":
                return this.getClass().getResource("/img/approved.png").toString();
            default:
                return null;
        }
    }
}
