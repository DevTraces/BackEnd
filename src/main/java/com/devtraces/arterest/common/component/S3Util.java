package com.devtraces.arterest.common.component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.devtraces.arterest.common.exception.BaseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Util {

	private static final Set<String> AVAILABLE_IMAGE_EXTENSION = new HashSet<>(Arrays.asList("gif", "png", "jpg", "jpeg", "bmp", "webp"));

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

			amazonS3.putObject(
				new PutObjectRequest(bucket, s3FileName, inputStream, objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (IOException e) {
			log.error(e.getMessage());
			throw BaseException.FAILED_FILE_UPLOAD;
		}

		return amazonS3.getUrl(bucket, s3FileName).toString();
	}

	public void deleteImage(String imageUrl) {
		try {
			String decodingUrl = URLDecoder.decode(imageUrl, "UTF-8");	// URL 디코딩
			int startIdx = decodingUrl.indexOf(directory);	// directory 경로가 처음 나온 부분부터 Object Key에 해당
			String objectKey = decodingUrl.substring(startIdx);
			amazonS3.deleteObject(bucket, objectKey);
		} catch (UnsupportedEncodingException | AmazonServiceException e) {
			log.error(e.getMessage());
			throw BaseException.INTERNAL_SERVER_ERROR;
		}
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
