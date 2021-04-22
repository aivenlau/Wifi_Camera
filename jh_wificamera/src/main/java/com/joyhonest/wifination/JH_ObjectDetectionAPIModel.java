/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.joyhonest.wifination;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * github.com/tensorflow/models/tree/master/research/object_detection
 */
public class JH_ObjectDetectionAPIModel implements Classifier {
  private static final Logger LOGGER = new Logger();
  // Only return this many results.
  private static final int MAX_RESULTS = 100;
  // Config values.
  private String inputName;
  //private int inputSize;
  private int inputW;
  private int inputH;

  // Pre-allocated buffers.
  private Vector<String> labels = new Vector<String>();
  private int[] intValues;
  private byte[] byteValues;
  private float[] outputLocations;
  private float[] outputScores;
  private float[] outputClasses;
  private float[] outputNumDetections;
  private String[] outputNames;

  private boolean logStats = false;

  private TensorFlowInferenceInterface inferenceInterface;

  /**
   * Initializes a native TensorFlow session for classifying images.
   *
   * @param assetManager The asset manager to be used to load assets.
   * @param modelFilename The filepath of the model GraphDef protocol buffer.
   * @param labelFilename The filepath of label file for classes.
   */

  //final int inputSize
  public static Classifier create(
          final AssetManager assetManager,
          final String modelFilename,
          final String labelFilename,
          final int inputW,final  int inputH) throws IOException {
    final JH_ObjectDetectionAPIModel dObject = new JH_ObjectDetectionAPIModel();

    InputStream labelsInput = null;
    String actualFilename = labelFilename.split("file:///android_asset/")[1];
    if(actualFilename!=null) {
      if(assetManager!=null)
        labelsInput = assetManager.open(actualFilename);
    }

    if(labelsInput==null)
    {
      labelsInput = new FileInputStream(labelFilename);
    }
    BufferedReader br = null;
    br = new BufferedReader(new InputStreamReader(labelsInput));
    String line;
    while ((line = br.readLine()) != null) {
      LOGGER.w(line);
      dObject.labels.add(line);
    }
    br.close();


    dObject.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

    final Graph graph = dObject.inferenceInterface.graph();

    dObject.inputName = "image_tensor";
    // The inputName node has a shape of [N, H, W, C], where
    // N is the batch size
    // H = W are the height and width
    // C is the number of channels (3 for our purposes - RGB)
    final Operation inputOp = graph.operation(dObject.inputName);
    if (inputOp == null) {
      throw new RuntimeException("Failed to find input Node '" + dObject.inputName + "'");
    }
    //dObject.inputSize = inputSize;
    dObject.inputW = inputW;
    dObject.inputH = inputH;
    // The outputScoresName node has a shape of [N, NumLocations], where N
    // is the batch size.
    final Operation outputOp1 = graph.operation("detection_scores");
    if (outputOp1 == null) {
      throw new RuntimeException("Failed to find output Node 'detection_scores'");
    }
    final Operation outputOp2 = graph.operation("detection_boxes");
    if (outputOp2 == null) {
      throw new RuntimeException("Failed to find output Node 'detection_boxes'");
    }
    final Operation outputOp3 = graph.operation("detection_classes");
    if (outputOp3 == null) {
      throw new RuntimeException("Failed to find output Node 'detection_classes'");
    }

    // Pre-allocate buffers.
    dObject.outputNames = new String[] {"detection_boxes", "detection_scores",
            "detection_classes", "num_detections"};
//    dObject.intValues = new int[dObject.inputSize * dObject.inputSize];
//    dObject.byteValues = new byte[dObject.inputSize * dObject.inputSize * 3];

    dObject.intValues = new int[dObject.inputW * dObject.inputH];
    dObject.byteValues = new byte[dObject.inputW * dObject.inputH * 3];

    dObject.outputScores = new float[MAX_RESULTS];
    dObject.outputLocations = new float[MAX_RESULTS * 4];
    dObject.outputClasses = new float[MAX_RESULTS];
    dObject.outputNumDetections = new float[1];
    return dObject;
  }

  private JH_ObjectDetectionAPIModel() {}

  @Override
  public List<Recognition> recognizeImage(final Bitmap bitmap) {
    // Log this method so that it can be analyzed with systrace.

    // Preprocess the image data to extract R, G and B bytes from int of form 0x00RRGGBB
    // on the provided parameters.

    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    for (int i = 0; i < intValues.length; ++i) {
      byteValues[i * 3 + 2] = (byte) (intValues[i] & 0xFF);
      byteValues[i * 3 + 1] = (byte) ((intValues[i] >> 8) & 0xFF);
      byteValues[i * 3 + 0] = (byte) ((intValues[i] >> 16) & 0xFF);
    }


    // Copy the input data into TensorFlow.

//    inferenceInterface.feed(inputName, byteValues, 1, inputSize, inputSize, 3);

    inferenceInterface.feed(inputName, byteValues, 1, inputW, inputH, 3);

    // Run the inference call.

    inferenceInterface.run(outputNames, logStats);


    // Copy the output Tensor back into the output array.

    outputLocations = new float[MAX_RESULTS * 4];
    outputScores = new float[MAX_RESULTS];
    outputClasses = new float[MAX_RESULTS];
    outputNumDetections = new float[1];
    inferenceInterface.fetch(outputNames[0], outputLocations);
    inferenceInterface.fetch(outputNames[1], outputScores);
    inferenceInterface.fetch(outputNames[2], outputClasses);
    inferenceInterface.fetch(outputNames[3], outputNumDetections);


    // Find the best detections.


    final PriorityQueue<Recognition> pq =
            new PriorityQueue<Recognition>(
                    1,
                    new Comparator<Recognition>() {
                      @Override
                      public int compare(final Recognition lhs, final Recognition rhs) {
                        // Intentionally reversed to put high confidence at the head of the queue.
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                      }
                    });

    // Scale them back to the input size.
    for (int i = 0; i < outputScores.length; ++i) {
//      final RectF detection =
//              new RectF(
//                      outputLocations[4 * i + 1] * inputSize,
//                      outputLocations[4 * i] * inputSize,
//                      outputLocations[4 * i + 3] * inputSize,
//                      outputLocations[4 * i + 2] * inputSize);

      final RectF detection =
              new RectF(
                      outputLocations[4 * i + 1] * inputW,
                      outputLocations[4 * i] * inputH,
                      outputLocations[4 * i + 3] * inputW,
                      outputLocations[4 * i + 2] * inputH);
      pq.add(
              new Recognition("" + i, labels.get((int) outputClasses[i]), outputScores[i], detection));
    }

    final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
    for (int i = 0; i < Math.min(pq.size(), MAX_RESULTS); ++i) {
      recognitions.add(pq.poll());
    }

    return recognitions;
  }

  @Override
  public void enableStatLogging(final boolean logStats) {
    this.logStats = logStats;
  }

  @Override
  public String getStatString() {
    return inferenceInterface.getStatString();
  }

  @Override
  public void close() {
    inferenceInterface.close();
  }
}
