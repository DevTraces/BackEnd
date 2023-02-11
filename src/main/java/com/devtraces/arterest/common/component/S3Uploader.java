package com.devtraces.arterest.common.component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.devtraces.arterest.common.exception.BaseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class S3Uploader {

	private static final Set<String> AVAILABLE_IMAGE_EXTENSION = new HashSet<>(Arrays.asList("gif", "png", "jpeg", "bmp", "webp", "jpg"));

	private final AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	@Value("${cloud.aws.s3.directory}")
	private String directory;

	public String uploadImage(MultipartFile multipartFile) {
		validateImageFileName(Objects.requireNonNull(multipartFile.getOriginalFilename()));
		String s3FileName = createS3FileName(multipartFile.getOriginalFilename());

		try (InputStream inputStream = multipartFile.getInputStream()) {
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(inputStream.available());
			objectMetadata.setContentType(multipartFile.getContentType());

			amazonS3.putObject(new PutObjectRequest(bucket, s3FileName, inputStream, objectMetadata)
				.withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (IOException e) {
			throw BaseException.FAILED_FILE_UPLOAD;
		}

		return amazonS3.getUrl(bucket, s3FileName).toString();
	}

	private void validateImageFileName(String fileName) {
		String[] splitElements = fileName.split("\\.");
		String fileExtension = splitElements[splitElements.length - 1];

		if (!AVAILABLE_IMAGE_EXTENSION.contains(fileExtension)) {
			throw BaseException.INVALID_IMAGE_EXTENSION;
		}
	}

	private String createS3FileName(String fileName) {
		return directory + "/" + UUID.randomUUID() + fileName;
	}
}
