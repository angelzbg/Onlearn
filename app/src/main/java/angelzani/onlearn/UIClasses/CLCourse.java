package angelzani.onlearn.UIClasses;

import android.content.Context;
import android.support.constraint.ConstraintLayout;

public class CLCourse extends ConstraintLayout {

    public String courseId, description, lecturerId;

    public CLCourse(Context context, String courseId, String description, String lecturerId) {
        super(context);

        this.courseId = courseId;
        this.description=description;
        this.lecturerId=lecturerId;
    }

}
