package datagenerator;

public class List{
    public Object data;
    List next;

    public List(){
        data = null;
        next = null;
}

    public List (Object some_data){
        data = some_data;
        next = null;
    }

    public List (Object some_data, List a_ptr){
        data = some_data;
        next = a_ptr;
    }


}
