package com.lms.mapper;

import com.lms.dto.course.CourseResponse;
import com.lms.dto.course.PublicCourseResponse;
import com.lms.model.Course;
import com.lms.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "instructor", source = "instructors", qualifiedByName = "getMainInstructor")
    @Mapping(target = "isPublished", expression = "java(course.getStatus() == Course.CourseStatus.PUBLISHED)")
    CourseResponse toCourseResponse(Course course);

    Course toCourse(CourseResponse courseResponse);

    @Mapping(target = "instructor", source = "instructors", qualifiedByName = "getMainInstructorWithBio")
    PublicCourseResponse toPublicCourseResponse(Course course);

    @Named("getMainInstructor")
    default CourseResponse.InstructorDTO getMainInstructor(Set<User> instructors) {
        if (instructors == null || instructors.isEmpty()) {
            return null;
        }
        User instructor = instructors.iterator().next();
        return new CourseResponse.InstructorDTO(
                instructor.getId(),
                instructor.getFirstName() + " " + instructor.getLastName(),
                instructor.getAvatar()
        );
    }

    @Named("getMainInstructorWithBio")
    default PublicCourseResponse.InstructorDTO getMainInstructorWithBio(Set<User> instructors) {
        if (instructors == null || instructors.isEmpty()) {
            return null;
        }
        User instructor = instructors.iterator().next();
        return new PublicCourseResponse.InstructorDTO(
                instructor.getId(),
                instructor.getFirstName() + " " + instructor.getLastName(),
                "Experienced instructor", // This would come from a profile bio field
                instructor.getAvatar()
        );
    }
}