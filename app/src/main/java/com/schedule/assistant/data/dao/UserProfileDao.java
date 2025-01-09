package com.schedule.assistant.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.schedule.assistant.data.entity.UserProfile;

@Dao
public interface UserProfileDao {
    @Insert
    void insert(UserProfile userProfile);

    @Update
    void update(UserProfile userProfile);

    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfile getUserProfile();

    @Query("UPDATE user_profile SET name = :name, imageUri = :imageUri, email = :email WHERE id = :id")
    void updateProfile(int id, String name, String imageUri, String email);
}