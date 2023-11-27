package com.divtech.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;


/**
 * Entity class representing user information stored in the database.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_tbl")
public class User {

    @Id
    private String id;                          // Unique identifier for the user
    private String name;                        // User's name
    private String address;                     // User's address
    private String phone;                       // User's phone number
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAccessTime;                // Timestamp indicating the user's last access time
}
