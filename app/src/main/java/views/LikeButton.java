package views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.solojet.onefoldtv.R;

@SuppressLint("AppCompatCustomView")
public class LikeButton extends ImageView {
    String TAG = LikeButton.class.getSimpleName();

    public static final int LIKED = 1;
    public static final int NOT_LIKED = -1;

    private int state;
    public LikeButton(Context context) {
        super(context);
    }

    public LikeButton(Context context, AttributeSet attr){
        super(context, attr);
    }

    private void init() {
        switch (state){
            case LIKED:
                setImageResource(R.drawable.ic_favorite_black_24dp);
                break;
            default:
                setImageResource(R.drawable.ic_favorite_border_black_24dp);
                break;
        }
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        init();
    }
}

