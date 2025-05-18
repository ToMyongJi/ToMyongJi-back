package com.example.tomyongji.auth.mapper;

import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.receipt.entity.StudentClub;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-13T17:07:56+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserRequestDto dto, StudentClub studentClub) {
        if ( dto == null && studentClub == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        if ( dto != null ) {
            user.collegeName( dto.getCollegeName() );
            user.userId( dto.getUserId() );
            user.name( dto.getName() );
            user.studentNum( dto.getStudentNum() );
            user.email( dto.getEmail() );
            user.password( dto.getPassword() );
            user.role( dto.getRole() );
        }
        if ( studentClub != null ) {
            user.studentClub( studentClub );
            user.id( studentClub.getId() );
        }

        return user.build();
    }
}
