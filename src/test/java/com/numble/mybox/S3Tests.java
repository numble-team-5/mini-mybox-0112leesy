package com.numble.mybox;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultipartUpload;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
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
        .withCredentials(
            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
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
    public void putBucketTest() {
        // generate unique bucket names using UUID
        String newBucketName = UUID.randomUUID().toString();

        try {
            // create bucket if the bucket name does not exist
            if (s3.doesBucketExistV2(newBucketName)) {
                System.out.format("Bucket %s already exists.\n", newBucketName);
            } else {
                s3.createBucket(newBucketName);
                System.out.format("Bucket %s has been created.\n", newBucketName);
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteBucketTest() {
        String bucketName = "b7bf8e10-73f2-4a9c-bc17-8c24aaa7d5b7";

        try {
            // delete bucket if the bucket exists
            if (s3.doesBucketExistV2(bucketName)) {
                // delete all objects
                ObjectListing objectListing = s3.listObjects(bucketName);
                while (true) {
                    for (Iterator<?> iterator = objectListing.getObjectSummaries().iterator(); iterator.hasNext();) {
                        S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
                        s3.deleteObject(bucketName, summary.getKey());
                    }

                    if (objectListing.isTruncated()) {
                        objectListing = s3.listNextBatchOfObjects(objectListing);
                    } else {
                        break;
                    }
                }

                // abort incomplete multipart uploads
                MultipartUploadListing multipartUploadListing = s3.listMultipartUploads(new ListMultipartUploadsRequest(bucketName));
                while (true) {
                    for (Iterator<?> iterator = multipartUploadListing.getMultipartUploads().iterator(); iterator.hasNext();) {
                        MultipartUpload multipartUpload = (MultipartUpload)iterator.next();
                        s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, multipartUpload.getKey(), multipartUpload.getUploadId()));
                    }

                    if (multipartUploadListing.isTruncated()) {
                        ListMultipartUploadsRequest listMultipartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
                        listMultipartUploadsRequest.withUploadIdMarker(multipartUploadListing.getNextUploadIdMarker());
                        multipartUploadListing = s3.listMultipartUploads(listMultipartUploadsRequest);
                    } else {
                        break;
                    }
                }

                s3.deleteBucket(bucketName);
                System.out.format("Bucket %s has been deleted.\n", bucketName);
            } else {
                System.out.format("Bucket %s does not exist.\n", bucketName);
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
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
                    System.out.println(
                        "    name=" + objectSummary.getKey() + ", size=" + objectSummary.getSize()
                            + ", owner=" + objectSummary.getOwner().getId());
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

    @Test
    public void getRootObjectTest() {
        // top level folders and files in the bucket
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withDelimiter("/")
                .withMaxKeys(300);

            ObjectListing objectListing = s3.listObjects(listObjectsRequest);

            System.out.println("Folder List:");
            for (String commonPrefixes : objectListing.getCommonPrefixes()) {
                System.out.println("    name=" + commonPrefixes);
            }

            System.out.println("File List:");
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(
                    "    name=" + objectSummary.getKey() + ", size=" + objectSummary.getSize()
                        + ", owner=" + objectSummary.getOwner().getId());
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createFolderTest() {
        // create folder
        String folderName = "sample-folder/";

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName,
            new ByteArrayInputStream(new byte[0]), objectMetadata);

        try {
            s3.putObject(putObjectRequest);
            System.out.format("Folder %s has been created.\n", folderName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        // create folder under folder
        folderName = "폴더1/depth-2/";

        objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        putObjectRequest = new PutObjectRequest(bucketName, folderName,
            new ByteArrayInputStream(new byte[0]), objectMetadata);

        try {
            s3.putObject(putObjectRequest);
            System.out.format("Folder %s has been created.\n", folderName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void uploadTextFileTest() {
        // upload local file
        String objectName = "sample-object.txt";
        String filePath = "C:\\Users\\LGgram\\Desktop\\test1.txt";

        try {
            s3.putObject(bucketName, objectName, new File(filePath));
            System.out.format("Object %s has been created.\n", objectName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void uploadImageFileTest() {
        // upload local file
        String objectName = "sample-image-object.jpg";
        String filePath = "C:\\Users\\LGgram\\Desktop\\testImage1.jpeg";

        try {
            s3.putObject(bucketName, objectName, new File(filePath));
            System.out.format("Object %s has been created.\n", objectName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void downloadTextFileTest() {
        String objectName = "test1.txt";
        String downloadFilePath = "C:\\Users\\LGgram\\Desktop\\text-download.txt";

        // download object
        try {
            S3Object s3Object = s3.getObject(bucketName, objectName);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();

            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFilePath));
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = s3ObjectInputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
            }

            outputStream.close();
            s3ObjectInputStream.close();
            System.out.format("Object %s has been downloaded.\n", objectName);
        } catch (AmazonS3Exception e) {
            System.out.println("AmazonS3Exception");
            e.printStackTrace();
        } catch(SdkClientException e) {
            System.out.println("SdkClientException");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("IOException");
            throw new RuntimeException(e);
        }
    }

    @Test
    public void downloadImageFileTest() {
        String objectName = "폴더1/맑은 날 한강.jpeg";
        String downloadFilePath = "C:\\Users\\LGgram\\Desktop\\image-download.jpeg";

        // download object
        try {
            S3Object s3Object = s3.getObject(bucketName, objectName);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();

            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFilePath));
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = s3ObjectInputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
            }

            outputStream.close();
            s3ObjectInputStream.close();
            System.out.format("Object %s has been downloaded.\n", objectName);
        } catch (AmazonS3Exception e) {
            System.out.println("AmazonS3Exception");
            e.printStackTrace();
        } catch(SdkClientException e) {
            System.out.println("SdkClientException");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("IOException");
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteFileTest() {
        String objectName = "sample-object.txt";

        // delete object
        try {
            s3.deleteObject(bucketName, objectName);
            System.out.format("Object %s has been deleted.\n", objectName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

}
