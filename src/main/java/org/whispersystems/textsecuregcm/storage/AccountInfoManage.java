package org.whispersystems.textsecuregcm.storage;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.whispersystems.textsecuregcm.entities.AccountInfo;

public abstract class AccountInfoManage {
    
    public static class AccountInfoMapper implements ResultSetMapper<AccountInfo> {
        @Override
        public AccountInfo map(int i, ResultSet resultSet, StatementContext statementContext)
            throws SQLException
        {
          return new AccountInfo(resultSet.getLong("id"), resultSet.getString("number"), resultSet.getString("nickname"),
                             resultSet.getBoolean("gender"), resultSet.getInt("age"),
                             resultSet.getString("work"),resultSet.getLong("imageattachmentid"), resultSet.getString("sign"));
        }
      }
    
    @BindingAnnotation(AccountInfoBinder.AccountInfoBinderFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface AccountInfoBinder {
      public static class AccountInfoBinderFactory implements BinderFactory {
        @Override
        public Binder build(Annotation annotation) {
          return new Binder<AccountInfoBinder, AccountInfo>() {
            @Override
            public void bind(SQLStatement<?> sql, AccountInfoBinder Binder, AccountInfo info)
            {
              sql.bind("id", info.getId());
              sql.bind("number", info.getNumber());
              sql.bind("nickname", info.getNickname());
              sql.bind("gender", info.getGender());
              sql.bind("age", info.getAge());
              sql.bind("work", info.getWork());
              sql.bind("imageattachmentid", info.getImageattachmentid());
              sql.bind("sign", info.getSign());
            }
          };
        }
      }
    }
    
    @SqlUpdate("DELETE FROM accountsinfo WHERE number = :number")
    public abstract void removebynumber(@Bind("number") String number);
    
    @SqlUpdate("INSERT INTO accountsinfo (number,nickname,gender,age,work,imageattachmentid,sign) VALUES (:number,:nickname,:gender,:age,:work,:imageattachmentid,:sign)")
    @GetGeneratedKeys
    public abstract long insert(@AccountInfoBinder AccountInfo accountinfo);
    
    @SqlUpdate("UPDATE accountsinfo SET  nickname=:nickname,gender=:gender,age=:age,work=:work,imageattachmentid=:imageattachmentid,sign=:sign WHERE number = :number")
    public abstract void updatebynumber(@AccountInfoBinder AccountInfo accountinfo);
    
    @Mapper(AccountInfoMapper.class)
    @SqlQuery("SELECT * FROM accountsinfo WHERE  number = :number")
    public abstract AccountInfo getbynumber(@Bind("number") String number);
}


