package com.serendio.aws;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;


public class S3Interface {

    private static String bucketName = "izeadna";
    private static String key        = "*** provide object key ***";
    static AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider());


    public static void main(String[] args) throws IOException
    {
        S3Interface obj = new S3Interface();
        obj.listBucketObjects("izeadna","");
    }

    void listBucketObjects(String bucketname,String prefix)
    {
        ListObjectsRequest listObjectsRequest = null;
        if(prefix.isEmpty() == true)
            listObjectsRequest = new ListObjectsRequest().withBucketName(bucketname);
        else
            listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix);
        ObjectListing objectListing;
        do
        {
            objectListing = s3Client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary :objectListing.getObjectSummaries())
            {
                System.out.println( " - " + objectSummary.getKey() + "  " +"(size = " + objectSummary.getSize() +")");
                printObjectFromBucket(bucketName,objectSummary.getKey());
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
    }

    void printObjectFromBucket(String bucketName,String key)
    {
        try
        {
            System.out.println("Downloading an object");
            S3Object s3object = s3Client.getObject(new GetObjectRequest(
                    bucketName, key));
            System.out.println("Content-Type: "  +
                    s3object.getObjectMetadata().getContentType());
            displayTextInputStream(s3object.getObjectContent());

            // Get a range of bytes from an object.

            GetObjectRequest rangeObjectRequest = new GetObjectRequest(
                    bucketName, key);
            rangeObjectRequest.setRange(0, 10);
            S3Object objectPortion = s3Client.getObject(rangeObjectRequest);

            System.out.println("Printing bytes retrieved.");
            displayTextInputStream(objectPortion.getObjectContent());

        } catch (AmazonServiceException ase)
        {
            System.out.println("Caught an AmazonServiceException, which" +
                    " means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace)
        {
            System.out.println("Caught an AmazonClientException, which means"+
                    " the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

    }

    void displayTextInputStream(InputStream input)throws IOException
    {
        // Read one text line at a time and display.
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }
}