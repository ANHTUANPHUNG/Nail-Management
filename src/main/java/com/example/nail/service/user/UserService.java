package com.example.nail.service.user;


import com.example.nail.domain.*;
import com.example.nail.domain.eNum.ELock;
import com.example.nail.domain.eNum.ERole;
import com.example.nail.domain.eNum.EType;
import com.example.nail.exception.ResourceNotFoundException;
import com.example.nail.repository.ComboProductRepository;
import com.example.nail.repository.ComboRepository;
import com.example.nail.repository.FileRepository;
//import com.example.nail.repository.UserRepository;
import com.example.nail.repository.UserRepository;
import com.example.nail.service.combo.request.ComboEditRequest;
import com.example.nail.service.combo.request.ComboSaveRequest;
import com.example.nail.service.combo.response.ComboEditResponse;
import com.example.nail.service.request.SelectOptionRequest;
import com.example.nail.service.response.SelectOptionResponse;
import com.example.nail.service.user.request.UserEditRequest;
import com.example.nail.service.user.request.UserCreateRequest;
import com.example.nail.service.user.response.UserEditResponse;
import com.example.nail.service.user.response.UserListResponse;
import com.example.nail.util.AppMessage;
import com.example.nail.util.AppUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional

public class UserService {
    private FileRepository fileRepository;
    private UserRepository userRepository;
    public Optional<User> findByNameIgnoreCaseOrEmailIgnoreCaseOrPhone(String loginName) {
        return Optional.ofNullable(userRepository.findByNameIgnoreCaseOrEmailIgnoreCaseOrPhone(loginName, loginName, loginName)
                .orElseThrow(() -> new ResourceNotFoundException
                        (String.format(AppMessage.ID_NOT_FOUND, "User"))));
    }
    public List<SelectOptionResponse> findAll(){
        return userRepository.findAllByDeletedFalse().stream().map(
                e-> new SelectOptionResponse(e.getId().toString(),e.getUserName()))
                .collect(Collectors.toList());
    }
    public Page<UserListResponse> findAllUser(String search, Pageable pageable){
        return userRepository.searchAllByUserName(search,pageable)
                .map(e->UserListResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .phone(e.getPhone())
                        .email(e.getEmail())
                        .dob(String.valueOf(e.getDob()))
                        .type(String.valueOf(e.getType()))
                        .userName(e.getUserName())
                        .avatar(e.getAvatar().getFileUrl())
                        .lock(String.valueOf(e.getELock()))
                        .role(String.valueOf(e.getERole()))
                        .build());
    }
    public User findById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException
                (String.format(AppMessage.ID_NOT_FOUND, "User", id)));
    }
    public UserEditResponse showEdit(Long id){
        var user = findById(id);
        var userEditResponse = AppUtil.mapper.map(user,UserEditResponse.class);
        userEditResponse.setAvatar(user.getAvatar().getFileUrl());
        userEditResponse.setIdAvatar(user.getAvatar().getId());
        return userEditResponse;
    }
    public User create(UserCreateRequest userCreateRequest){
        User user = AppUtil.mapper.map(userCreateRequest, User.class);
        File avatar = fileRepository.findById(userCreateRequest.getAvatar().getId()).get();
        user.setAvatar(avatar);
        user.setDeleted(false);
        user.setELock(ELock.UNLOCK);
        user.setERole(ERole.ROLE_USER);
        user.setType(EType.SILVER);
        return userRepository.save(user);
    }
    public void update(UserEditRequest userEditRequest, Long id){
        var userDB = findById(id);
        userDB.setName(userEditRequest.getName());
        userDB.setPhone(userEditRequest.getPhone());
        userDB.setDob(LocalDate.parse(userEditRequest.getDob()));
        if(userEditRequest.getAvatar()!= null && userEditRequest.getAvatar().getId()!= null){
            userDB.setAvatar(File.builder().id(userEditRequest.getAvatar().getId()).build());
        }
//        Xét toàn bộ combo_id ở file về null trước khi thêm lại
        userRepository.save(userDB);

    }
    public void lockById(Long id){
        User user = findById(id);
        user.setDeleted(true);
        user.setELock(ELock.LOCK);
        userRepository.save(user);
    }
    public void unlockById(Long id){
        User user = findById(id);
        user.setDeleted(false);
        user.setELock(ELock.UNLOCK);
        userRepository.save(user);
    }
    public void roleAdmin(Long id){
        User user = findById(id);
        user.setERole(ERole.ROLE_ADMIN);
        userRepository.save(user);
    }
    public void roleUser(Long id){
        User user = findById(id);
        user.setERole(ERole.ROLE_USER);
        userRepository.save(user);
    }
}
