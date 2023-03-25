package com.numble.mybox.service.impl;

import com.numble.mybox.data.entity.Object;
import com.numble.mybox.data.entity.QObject;
import com.numble.mybox.data.repository.ObjectRepository;
import com.numble.mybox.service.ObjectService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObjectServiceImpl implements ObjectService {

    private final Logger LOGGER = LoggerFactory.getLogger(ObjectServiceImpl.class);
    // private final AmazonS3 amazonS3;
    private final ObjectRepository objectRepository;
    private final JPAQueryFactory queryFactory;

    @Autowired
    public ObjectServiceImpl(ObjectRepository objectRepository, JPAQueryFactory queryFactory) {
        this.objectRepository = objectRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Object> getRootObject(String bucketName) {
        QObject qObject = QObject.object;

        List<Object> objectList = queryFactory.selectFrom(qObject)
            .where(
                qObject.bucketName.eq(bucketName),
                qObject.parentFullName.isNull())
            .fetch();

        return objectList;
    }
}
