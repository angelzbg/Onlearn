package angelzani.onlearn.UIClasses;

import android.content.Context;
import android.support.constraint.ConstraintLayout;

public class CLCourse extends ConstraintLayout {

    private String courseId, description, lecturerId;

    public CLCourse(Context context, String courseId, String description, String lecturerId) {
        super(context);

        this.courseId = courseId;
        this.description=description;
        this.lecturerId=lecturerId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }
}
