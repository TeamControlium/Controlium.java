package TeamControlium.Controlium;

//
// Java doesnt have 'out' or 'ref' keywords for passing parameters to functions and allowing functions to modify the paramters.
// So, we create a warpper to do the job of wrapping a paramters allwing us to change it
//
//
public class ByReference<E> {
    private E byReference;
    public ByReference( E e ){
        byReference = e;
    }
    public ByReference(){
        byReference = null;
    }
    public E get() { return byReference; }
    public E set( E e ){ this.byReference = e; return this.byReference; }

    public String toString() {
        return byReference.toString();
    }
}
