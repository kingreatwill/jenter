package com.hawk.x.tracing.concept.sql;

import com.hawk.x.tracing.logging.Logging;
import com.hawk.x.tracing.op.NamedOf;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import javax.sql.DataSource;
import java.util.Properties;

// Executor (update, query, flushStatements, commit, rollback,getTransaction, close, isClosed)
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class , RowBounds.class, ResultHandler.class})
})
public class TracingInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        // 获取sql;
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        // 参数;
        Object parameterObject = boundSql.getParameterObject();
        // 连接池配置信息(使用信息?);
        DataSource stats = mappedStatement.getConfiguration().getEnvironment().getDataSource();
        // sql类型;
        SqlCommandType commandType = mappedStatement.getSqlCommandType();
        // 放置锚点;
        Span span = GlobalTracer.get().buildSpan(NamedOf.sql(commandType.name())).start();
        // 写入日志;
        Logging.of(span).log("",commandType.name(),boundSql.getSql(),stats,null,parameterObject);
        try {
            return invocation.proceed();
        } finally {
            span.finish();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
