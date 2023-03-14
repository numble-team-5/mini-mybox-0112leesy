package com.numble.mybox;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

public class S3Tests {

    final String endPoint = "https://kr.object.ncloudstorage.com";
    final String regionName = "kr-standard";
    final String accessKey = "9Pzg3ZVTiF5OJQiAz6JI";
    final String secretKey = "tx3kg9Dwaam8RWClQh7EXmq9sSRJODOoSzVYJmXg";
    final String bucketName = "mybox-test";

    // S3 client
    final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
        .build();

    @Test
    public void listBucketTest() {
        try {
            List<Bucket> buckets = s3.listBuckets();
            System.out.println("Bucket List: ");
            for (Bucket bucket : buckets) {
                System.out.println(
                    "    name=" + bucket.getName() + ", creation_date=" + bucket.getCreationDate()
                        + ", owner=" + bucket.getOwner().getId());
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllObjectTest() {
        // list all in the bucket
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withMaxKeys(300);

            ObjectListing objectListing = s3.listObjects(listObjectsRequest);

            System.out.println("Object List:");
            while (true) {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    System.out.println("    name=" + objectSummary.getKey() + ", size=" + objectSummary.getSize() + ", owner=" + objectSummary.getOwner().getId());
                }

                if (objectListing.isTruncated()) {
                    objectListing = s3.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }
        } catch (AmazonS3Exception e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
    }

}
