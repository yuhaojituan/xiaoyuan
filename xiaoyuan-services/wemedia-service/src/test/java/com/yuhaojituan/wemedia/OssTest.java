package com.yuhaojituan.wemedia;

import com.yuhaojituan.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OssTest {
    @Autowired
    FileStorageService fileStorageService;

    @Value("${file.oss.web-site}")
    String webSite;

    @Test
    public void upload() throws FileNotFoundException {
//        FileInputStream inputStream = new FileInputStream(new File("C:\\Users\\zhang\\Pictures\\Camera Roll\\1.webp"));
//
//        String wemedia = fileStorageService.store("material", "1.webp", inputStream);
//        System.out.println(webSite + wemedia);

        fileStorageService.delete("material/2023/2/20230210/1.webp");
    }
    @Test
    public void delete(){

    }

}
