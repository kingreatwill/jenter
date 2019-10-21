package com.hawk.x.tracing.concept.sql;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;

// Executor (update, query, flushStatements, commit, rollback,getTransaction, close, isClosed)
// bean.setPlugins(new Interceptor[]{new TracingInterceptor()});
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class , RowBounds.class, ResultHandler.class})
})
public class TracingInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Span sp = GlobalTracer.get().buildSpan("sql").start();

        Object target = invocation.getTarget();
        StatementHandler statementHandler = (StatementHandler) target;

        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType commandType = ms.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];

        try {
            return invocation.proceed();
        } finally {
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();
            String methodName = invocation.getMethod().getName();
            sp.setTag("sql.method",methodName);
            sp.finish();
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
