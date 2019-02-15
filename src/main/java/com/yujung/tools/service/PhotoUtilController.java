package com.yujung.tools.service;

import com.drew.imaging.ImageProcessingException;
import com.yujung.tools.photoutils.PhotoFileMover;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;

@RestController
public class PhotoUtilController {
    
    @RequestMapping("/") 
    public String index() {
        return "base service";
    }

//    @RequestMapping(value = "/photo/organize", method = RequestMethod.POST)
//    public ResponseEntity<InputStreamSource> organizePhotos(@RequestParam String srcDir, @RequestParam String destDir, @RequestParam boolean testOnly) {
//        try {
//            PhotoFileMover mover = new PhotoFileMover(srcDir, destDir);
//            mover.sortByDate(testOnly);
//        } catch (IOException | ImageProcessingException | ParseException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }
    
    @RequestMapping(value = "/photo/organize")
    public StreamingResponseBody organizePhotos(@RequestParam String srcDir, @RequestParam String destDir, @RequestParam boolean testOnly, HttpServletResponse response) {
        return outputStream -> {
            try {
                PhotoFileMover mover = new PhotoFileMover(srcDir, destDir, outputStream);
                mover.sortByDate(testOnly);
            } catch (IOException | ImageProcessingException | ParseException e) {
                e.printStackTrace(new PrintStream(outputStream));
            }
        };
    }
}
