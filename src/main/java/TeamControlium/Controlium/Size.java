package TeamControlium.Controlium;

//
// Is there something not like this is Java already?
//
public class Size {
    private int _height;
    private int _width;

    public Size(int height, int width) {
        _height = height;
        _width = width;
    }

    public int getHeight() { return _height;};
    public int setHeight(int height) { _height=height; return getHeight();}
    public int getWidth() { return _width;};
    public int setWidth(int width) { _width=width; return getWidth();}
    public boolean equals(Size target) { return (target.getWidth()== getWidth() && target.getHeight()==getHeight());}
}
