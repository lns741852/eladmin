/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.system.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-22
 */
@Entity
@Getter
@Setter
@Table(name="sys_user")
public class User extends BaseEntity implements Serializable {

    @Id
    @Column(name = "user_id")
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @ApiModelProperty(value = "用戶角色")
    @JoinTable(name = "sys_users_roles",
            joinColumns = {@JoinColumn(name = "user_id",referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id",referencedColumnName = "role_id")})
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.EAGER)
    @ApiModelProperty(value = "用戶崗位")
    @JoinTable(name = "sys_users_jobs",
            joinColumns = {@JoinColumn(name = "user_id",referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "job_id",referencedColumnName = "job_id")})
    private Set<Job> jobs;

    @OneToOne
    @JoinColumn(name = "dept_id")
    @ApiModelProperty(value = "用戶部門")
    private Dept dept;

    @NotBlank
    @Column(unique = true)
    @ApiModelProperty(value = "用戶名稱")
    private String username;

    @NotBlank
    @ApiModelProperty(value = "用戶昵稱")
    private String nickName;

    @Email
    @NotBlank
    @ApiModelProperty(value = "郵箱")
    private String email;

    @NotBlank
    @ApiModelProperty(value = "手機號碼")
    private String phone;

    @ApiModelProperty(value = "用戶性別")
    private String gender;

    @ApiModelProperty(value = "頭像真實名稱",hidden = true)
    private String avatarName;

    @ApiModelProperty(value = "頭像存儲的路徑", hidden = true)
    private String avatarPath;

    @ApiModelProperty(value = "密碼")
    private String password;

    @NotNull
    @ApiModelProperty(value = "是否啟用")
    private Boolean enabled;

    @ApiModelProperty(value = "是否為admin賬號", hidden = true)
    private Boolean isAdmin = false;

    @Column(name = "pwd_reset_time")
    @ApiModelProperty(value = "最後修改密碼的時間", hidden = true)
    private Date pwdResetTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}