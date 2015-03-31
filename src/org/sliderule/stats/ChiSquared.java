/*
 * Copyright (C) 2015 Christopher Friedt <chrisfriedt@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sliderule.stats;

import java.util.*;

/**
 * <p><b>&Chi;&sup2; Distribution</b></p>
 *
 * <p>This class provides a time-efficient means for querying values and
 * inverse values from the
 * <a href="http://en.wikipedia.org/wiki/Cumulative_distribution_function">cumulative distribution function (CDF)</a>
 * for the
 * <a href="http://en.wikipedia.org/wiki/Chi-squared_distribution">Chi-Squared distribution</a>.
 * Also provided is a method to determine the minimum number of samples required, from a {@link Normal}
 * random variable, for a given
 * <a href="http://en.wikipedia.org/wiki/Confidence_interval">confidence level</a>
 * in determining that it is normally distributed.
 * Lastly, a method is provided to {@link #test} data for
 * <a href="http://en.wikipedia.org/wiki/Goodness_of_fit">goodness of fit</a>
 * to a particular distribution - i.e. to perform a
 * <a href="http://en.wikipedia.org/wiki/Chi-square_test">&Chi;&sup2; test</a></p>
 * </p>
 *
 * @author <a href="mailto:chrisfriedt@gmail.com">Christopher Friedt</a>
 * @see
 *   <ul>
 *     <li>Wikipedia: <a href=""http://en.wikipedia.org/wiki/Chi-squared_distribution">Student's t-distribution</a></li>
 *     <li>DeGroot, Morris H., and Schervish, Mark J. Probability and Statistics, 3rd Edition. Toronto: Addison-Wesley, 2002. pp. 393-404, 776-777. Print.</li>
 *   </ul>
 *
 */
public final class ChiSquared {

	private static final double[] xp = { 0.005, 0.01, 0.025, 0.05, 0.1, 0.2, 0.25, 0.3, 0.4, 0.5, 0.6, 0.7, 0.75, 0.8, 0.9, 0.95, 0.975, 0.99, 0.995, };
	// generated via Matlab's chi2inv function
	private static final double[][] xtable = {
		// each row in this represents a degree of freedom, i.e. xtable[0] is the array X values with one degree of freedom
		{ 0.000039, 0.000157, 0.000982, 0.003932, 0.015791, 0.064185, 0.101531, 0.148472, 0.274996, 0.454936, 0.708326, 1.074194, 1.323304, 1.642374, 2.705543, 3.841459, 5.023886, 6.634897, 7.879439,  },
		{ 0.010025, 0.020101, 0.050636, 0.102587, 0.210721, 0.446287, 0.575364, 0.713350, 1.021651, 1.386294, 1.832581, 2.407946, 2.772589, 3.218876, 4.605170, 5.991465, 7.377759, 9.210340, 10.596635,  },
		{ 0.071722, 0.114832, 0.215795, 0.351846, 0.584374, 1.005174, 1.212533, 1.423652, 1.869168, 2.365974, 2.946166, 3.664871, 4.108345, 4.641628, 6.251389, 7.814728, 9.348404, 11.344867, 12.838156,  },
		{ 0.206989, 0.297109, 0.484419, 0.710723, 1.063623, 1.648777, 1.922558, 2.194698, 2.752843, 3.356694, 4.044626, 4.878433, 5.385269, 5.988617, 7.779440, 9.487729, 11.143287, 13.276704, 14.860259,  },
		{ 0.411742, 0.554298, 0.831212, 1.145476, 1.610308, 2.342534, 2.674603, 2.999908, 3.655500, 4.351460, 5.131867, 6.064430, 6.625680, 7.289276, 9.236357, 11.070498, 12.832502, 15.086272, 16.749602,  },
		{ 0.675727, 0.872090, 1.237344, 1.635383, 2.204131, 3.070088, 3.454599, 3.827552, 4.570154, 5.348121, 6.210757, 7.231135, 7.840804, 8.558060, 10.644641, 12.591587, 14.449375, 16.811894, 18.547584,  },
		{ 0.989256, 1.239042, 1.689869, 2.167350, 2.833107, 3.822322, 4.254852, 4.671330, 5.493235, 6.345811, 7.283208, 8.383431, 9.037148, 9.803250, 12.017037, 14.067140, 16.012764, 18.475307, 20.277740,  },
		{ 1.344413, 1.646497, 2.179731, 2.732637, 3.489539, 4.593574, 5.070640, 5.527422, 6.422646, 7.344121, 8.350525, 9.524458, 10.218855, 11.030091, 13.361566, 15.507313, 17.534546, 20.090235, 21.954955,  },
		{ 1.734933, 2.087901, 2.700389, 3.325113, 4.168159, 5.380053, 5.898826, 6.393306, 7.357035, 8.342833, 9.413640, 10.656372, 11.388751, 12.242145, 14.683657, 16.918978, 19.022768, 21.665994, 23.589351,  },
		{ 2.155856, 2.558212, 3.246973, 3.940299, 4.865182, 6.179079, 6.737201, 7.267218, 8.295472, 9.341818, 10.473236, 11.780723, 12.548861, 13.441958, 15.987179, 18.307038, 20.483177, 23.209251, 25.188180,  },
		{ 2.603222, 3.053484, 3.815748, 4.574813, 5.577785, 6.988674, 7.584143, 8.147868, 9.237285, 10.340998, 11.529834, 12.898668, 13.700693, 14.631421, 17.275009, 19.675138, 21.920049, 24.724970, 26.756849,  },
		{ 3.073824, 3.570569, 4.403789, 5.226029, 6.303796, 7.807328, 8.438419, 9.034277, 10.181971, 11.340322, 12.583838, 14.011100, 14.845404, 15.811986, 18.549348, 21.026070, 23.336664, 26.216967, 28.299519,  },
		{ 3.565035, 4.106915, 5.008751, 5.891864, 7.041505, 8.633861, 9.299066, 9.925682, 11.129140, 12.339756, 13.635571, 15.118722, 15.983906, 16.984797, 19.811929, 22.362032, 24.735605, 27.688250, 29.819471,  },
		{ 4.074675, 4.660425, 5.628726, 6.570631, 7.789534, 9.467328, 10.165314, 10.821478, 12.078482, 13.339274, 14.685294, 16.222099, 17.116934, 18.150771, 21.064144, 23.684791, 26.118948, 29.141238, 31.319350,  },
		{ 4.600916, 5.229349, 6.262138, 7.260944, 8.546756, 10.306959, 11.036538, 11.721169, 13.029750, 14.338860, 15.733223, 17.321694, 18.245086, 19.310657, 22.307130, 24.995790, 27.488393, 30.577914, 32.801321,  },
		{ 5.142205, 5.812212, 6.907664, 7.961646, 9.312236, 11.152116, 11.912220, 12.624349, 13.982736, 15.338499, 16.779537, 18.417894, 19.368860, 20.465079, 23.541829, 26.296228, 28.845351, 31.999927, 34.267187,  },
		{ 5.697217, 6.407760, 7.564186, 8.671760, 10.085186, 12.002266, 12.791926, 13.530676, 14.937272, 16.338182, 17.824387, 19.511022, 20.488676, 21.614561, 24.769035, 27.587112, 30.191009, 33.408664, 35.718466,  },
		{ 6.264805, 7.014911, 8.230746, 9.390455, 10.864936, 12.856953, 13.675290, 14.439862, 15.893212, 17.337902, 18.867904, 20.601354, 21.604890, 22.759546, 25.989423, 28.869299, 31.526378, 34.805306, 37.156451,  },
		{ 6.843971, 7.632730, 8.906516, 10.117013, 11.650910, 13.715790, 14.561997, 15.351660, 16.850433, 18.337653, 19.910199, 21.689127, 22.717807, 23.900417, 27.203571, 30.143527, 32.852327, 36.190869, 38.582257,  },
		{ 7.433844, 8.260398, 9.590777, 10.850811, 12.442609, 14.578439, 15.451774, 16.265856, 17.808829, 19.337429, 20.951368, 22.774545, 23.827692, 25.037506, 28.411981, 31.410433, 34.169607, 37.566235, 39.996846,  },
		{ 8.033653, 8.897198, 10.282898, 11.591305, 13.239598, 15.444608, 16.344384, 17.182265, 18.768309, 20.337228, 21.991497, 23.857789, 24.934777, 26.171100, 29.615089, 32.670573, 35.478876, 38.932173, 41.401065,  },
		{ 8.642716, 9.542492, 10.982321, 12.338015, 14.041493, 16.314040, 17.239619, 18.100723, 19.728791, 21.337045, 23.030661, 24.939016, 26.039265, 27.301454, 30.813282, 33.924438, 36.780712, 40.289360, 42.795655,  },
		{ 9.260425, 10.195716, 11.688552, 13.090514, 14.847956, 17.186506, 18.137297, 19.021087, 20.690204, 22.336878, 24.068925, 26.018365, 27.141336, 28.428793, 32.006900, 35.172462, 38.075627, 41.638398, 44.181275,  },
		{ 9.886234, 10.856361, 12.401150, 13.848425, 15.658684, 18.061804, 19.037253, 19.943229, 21.652486, 23.336726, 25.106348, 27.095961, 28.241150, 29.553315, 33.196244, 36.415029, 39.364077, 42.979820, 45.558512,  },
		{ 10.519652, 11.523975, 13.119720, 14.611408, 16.473408, 18.939754, 19.939341, 20.867034, 22.615579, 24.336587, 26.142984, 28.171915, 29.338850, 30.675201, 34.381587, 37.652484, 40.646469, 44.314105, 46.927890,  },
		{ 11.160237, 12.198147, 13.843905, 15.379157, 17.291885, 19.820194, 20.843431, 21.792401, 23.579434, 25.336458, 27.178880, 29.246327, 30.434565, 31.794610, 35.563171, 38.885139, 41.923170, 45.641683, 48.289882,  },
		{ 11.807587, 12.878504, 14.573383, 16.151396, 18.113896, 20.702976, 21.749405, 22.719236, 24.544005, 26.336339, 28.214078, 30.319286, 31.528412, 32.911688, 36.741217, 40.113272, 43.194511, 46.962942, 49.644915,  },
		{ 12.461336, 13.564710, 15.307861, 16.927875, 18.939242, 21.587969, 22.657156, 23.647457, 25.509251, 27.336229, 29.248618, 31.390875, 32.620494, 34.026565, 37.915923, 41.337138, 44.460792, 48.278236, 50.993376,  },
		{ 13.121149, 14.256455, 16.047072, 17.708366, 19.767744, 22.475052, 23.566586, 24.576988, 26.475134, 28.336127, 30.282536, 32.461168, 33.710909, 35.139362, 39.087470, 42.556968, 45.722286, 49.587884, 52.335618,  },
		{ 13.786720, 14.953457, 16.790772, 18.492661, 20.599235, 23.364115, 24.477608, 25.507759, 27.441622, 29.336032, 31.315863, 33.530233, 34.799743, 36.250187, 40.256024, 43.772972, 46.979242, 50.892181, 53.671962,  },
		{ 14.457767, 15.655456, 17.538739, 19.280569, 21.433565, 24.255056, 25.390139, 26.439706, 28.408683, 30.335942, 32.348630, 34.598131, 35.887076, 37.359140, 41.421736, 44.985343, 48.231890, 52.191395, 55.002704,  },
		{ 15.134032, 16.362216, 18.290765, 20.071913, 22.270594, 25.147785, 26.304107, 27.372773, 29.376287, 31.335859, 33.380863, 35.664921, 36.972982, 38.466313, 42.584745, 46.194260, 49.480438, 53.485772, 56.328115,  },
		{ 15.815274, 17.073514, 19.046662, 20.866534, 23.110197, 26.042216, 27.219441, 28.306905, 30.344410, 32.335781, 34.412589, 36.730654, 38.057529, 39.571790, 43.745180, 47.399884, 50.725080, 54.775540, 57.648445,  },
		{ 16.501272, 17.789147, 19.806253, 21.664281, 23.952253, 26.938269, 28.136080, 29.242054, 31.313027, 33.335707, 35.443829, 37.795378, 39.140779, 40.675649, 44.903158, 48.602367, 51.965995, 56.060909, 58.963926,  },
		{ 17.191820, 18.508926, 20.569377, 22.465015, 24.796655, 27.835874, 29.053964, 30.178172, 32.282116, 34.335638, 36.474606, 38.859140, 40.222790, 41.777963, 46.058788, 49.801850, 53.203349, 57.342073, 60.274771,  },
		{ 17.886727, 19.232676, 21.335882, 23.268609, 25.643300, 28.734961, 29.973039, 31.115219, 33.251656, 35.335573, 37.504939, 39.921981, 41.303616, 42.878799, 47.212174, 50.998460, 54.437294, 58.619215, 61.581179,  },
		{ 18.585812, 19.960232, 22.105627, 24.074943, 26.492094, 29.635469, 30.893255, 32.053155, 34.221627, 36.335511, 38.534848, 40.983939, 42.383306, 43.978218, 48.363408, 52.192320, 55.667973, 59.892500, 62.883335,  },
		{ 19.288912, 20.691442, 22.878482, 24.883904, 27.342950, 30.537340, 31.814565, 32.991942, 35.192013, 37.335453, 39.564349, 42.045050, 43.461907, 45.076278, 49.512580, 53.383541, 56.895521, 61.162087, 64.181412,  },
		{ 19.995868, 21.426163, 23.654325, 25.695390, 28.195785, 31.440518, 32.736926, 33.931548, 36.162795, 38.335397, 40.593459, 43.105349, 44.539463, 46.173035, 50.659770, 54.572228, 58.120060, 62.428121, 65.475571,  },
		{ 20.706535, 22.164261, 24.433039, 26.509303, 29.050523, 32.344953, 33.660295, 34.871939, 37.133959, 39.335345, 41.622193, 44.164867, 45.616014, 47.268538, 51.805057, 55.758479, 59.341707, 63.690740, 66.765962,  },
		{ 21.420777, 22.905611, 25.214519, 27.325551, 29.907091, 33.250597, 34.584635, 35.813087, 38.105491, 40.335295, 42.650565, 45.223633, 46.691598, 48.362835, 52.948512, 56.942387, 60.560572, 64.950071, 68.052726,  },
		{ 22.138463, 23.650095, 25.998662, 28.144049, 30.765423, 34.157405, 35.509910, 36.754963, 39.077375, 41.335247, 43.678588, 46.281675, 47.766251, 49.455970, 54.090202, 58.124038, 61.776756, 66.206236, 69.335997,  },
		{ 22.859474, 24.397601, 26.785374, 28.964717, 31.625454, 35.065335, 36.436086, 37.697540, 40.049601, 42.335202, 44.706275, 47.339020, 48.840006, 50.547986, 55.230192, 59.303512, 62.990356, 67.459348, 70.615900,  },
		{ 23.583693, 25.148025, 27.574566, 29.787477, 32.487126, 35.974348, 37.363131, 38.640794, 41.022154, 43.335159, 45.733637, 48.395691, 49.912895, 51.638922, 56.368541, 60.480887, 64.201461, 68.709513, 71.892550,  },
		{ 24.311014, 25.901269, 28.366152, 30.612259, 33.350381, 36.884407, 38.291015, 39.584701, 41.995025, 44.335118, 46.760687, 49.451713, 50.984949, 52.728815, 57.505305, 61.656233, 65.410159, 69.956832, 73.166061,  },
		{ 25.041334, 26.657239, 29.160054, 31.438995, 34.215167, 37.795475, 39.219710, 40.529240, 42.968202, 45.335078, 47.787433, 50.507106, 52.056194, 53.817700, 58.640537, 62.829620, 66.616529, 71.201400, 74.436535,  },
		{ 25.774557, 27.415847, 29.956196, 32.267622, 35.081432, 38.707521, 40.149189, 41.474389, 43.941674, 46.335041, 48.813887, 51.561892, 53.126658, 54.905610, 59.774289, 64.001112, 67.820647, 72.443307, 75.704073,  },
		{ 26.510591, 28.177009, 30.754506, 33.098077, 35.949131, 39.620511, 41.079427, 42.420129, 44.915434, 47.335005, 49.840058, 52.616089, 54.196365, 55.992576, 60.906607, 65.170769, 69.022586, 73.682639, 76.968768,  },
		{ 27.249349, 28.940646, 31.554916, 33.930306, 36.818217, 40.534417, 42.010399, 43.366441, 45.889471, 48.334970, 50.865954, 53.669718, 55.265340, 57.078629, 62.037537, 66.338649, 70.222414, 74.919474, 78.230708,  },
		{ 27.990749, 29.706683, 32.357364, 34.764252, 37.688648, 41.449211, 42.942084, 44.313307, 46.863776, 49.334937, 51.891584, 54.722794, 56.333605, 58.163797, 63.167121, 67.504807, 71.420195, 76.153891, 79.489978,  },
		{ 28.734712, 30.475048, 33.161786, 35.599864, 38.560384, 42.364864, 43.874459, 45.260711, 47.838342, 50.334905, 52.916956, 55.775335, 57.401182, 59.248105, 64.295400, 68.669294, 72.615992, 77.385962, 80.746659,  },
		{ 29.481164, 31.245673, 33.968126, 36.437093, 39.433385, 43.281353, 44.807504, 46.208636, 48.813161, 51.334874, 53.942077, 56.827357, 58.468090, 60.331581, 65.422413, 69.832160, 73.809863, 78.615756, 82.000826,  },
		{ 30.230033, 32.018493, 34.776329, 37.275893, 40.307615, 44.198651, 45.741199, 47.157067, 49.788226, 52.334845, 54.966956, 57.878875, 59.534350, 61.414247, 66.548197, 70.993453, 75.001864, 79.843338, 83.252551,  },
		{ 30.981253, 32.793447, 35.586340, 38.116218, 41.183039, 45.116737, 46.675526, 48.105991, 50.763528, 53.334816, 55.991598, 58.929902, 60.599980, 62.496127, 67.672786, 72.153216, 76.192048, 81.068772, 84.501905,  },
		{ 31.734757, 33.570475, 36.398111, 38.958027, 42.059623, 46.035588, 47.610467, 49.055392, 51.739063, 54.334789, 57.016011, 59.980454, 61.664997, 63.577244, 68.796214, 73.311493, 77.380466, 82.292117, 85.748952,  },
		{ 32.490486, 34.349522, 37.211593, 39.801278, 42.937337, 46.955184, 48.546005, 50.005258, 52.714822, 55.334763, 58.040200, 61.030542, 62.729419, 64.657617, 69.918513, 74.468324, 78.567165, 83.513430, 86.993755,  },
		{ 33.248378, 35.130533, 38.026741, 40.645933, 43.816148, 47.875503, 49.482124, 50.955576, 53.690801, 56.334737, 59.064172, 62.080180, 63.793261, 65.737268, 71.039713, 75.623748, 79.752192, 84.732766, 88.236375,  },
		{ 34.008379, 35.913458, 38.843510, 41.491954, 44.696029, 48.796528, 50.418809, 51.906334, 54.666993, 57.334713, 60.087932, 63.129380, 64.856540, 66.816214, 72.159844, 76.777803, 80.935592, 85.950176, 89.476870,  },
		{ 34.770434, 36.698246, 39.661859, 42.339308, 45.576951, 49.718238, 51.356044, 52.857521, 55.643393, 58.334689, 61.111487, 64.178152, 65.919268, 67.894475, 73.278932, 77.930524, 82.117406, 87.165711, 90.715293,  },
		{ 35.534491, 37.484852, 40.481748, 43.187958, 46.458888, 50.640618, 52.293817, 53.809126, 56.619995, 59.334666, 62.134840, 65.226507, 66.981461, 68.972069, 74.397006, 79.081944, 83.297675, 88.379419, 91.951698,  },
		{ 36.300501, 38.273228, 41.303138, 44.037874, 47.341815, 51.563650, 53.232112, 54.761138, 57.596794, 60.334644, 63.157998, 66.274457, 68.043132, 70.049011, 75.514089, 80.232098, 84.476437, 89.591344, 93.186135,  },
		{ 37.068415, 39.063333, 42.125992, 44.889024, 48.225706, 52.487317, 54.170917, 55.713547, 58.573786, 61.334623, 64.180965, 67.322011, 69.104294, 71.125318, 76.630208, 81.381015, 85.653731, 90.801532, 94.418653,  },
		{ 37.838189, 39.855125, 42.950275, 45.741377, 49.110539, 53.411605, 55.110220, 56.666343, 59.550965, 62.334602, 65.203745, 68.369179, 70.164960, 72.201006, 77.745385, 82.528727, 86.829591, 92.010024, 95.649297,  },
		{ 38.609778, 40.648563, 43.775953, 46.594905, 49.996290, 54.336498, 56.050009, 57.619517, 60.528327, 63.334582, 66.226344, 69.415971, 71.225140, 73.276089, 78.859642, 83.675261, 88.004051, 93.216860, 96.878113,  },
		{ 39.383141, 41.443609, 44.602993, 47.449581, 50.882939, 55.261982, 56.990272, 58.573061, 61.505868, 64.334563, 67.248765, 70.462394, 72.284848, 74.350581, 79.973003, 84.820645, 89.177145, 94.422079, 98.105144,  },
		{ 40.158236, 42.240227, 45.431363, 48.305378, 51.770464, 56.188044, 57.930999, 59.526964, 62.483584, 65.334544, 68.271013, 71.508457, 73.344093, 75.424497, 81.085486, 85.964907, 90.348904, 95.625719, 99.330430,  },
		{ 40.935024, 43.038380, 46.261034, 49.162270, 52.658846, 57.114670, 58.872178, 60.481220, 63.461470, 66.334526, 69.293091, 72.554170, 74.402886, 76.497849, 82.197113, 87.108072, 91.519359, 96.827816, 100.554011,  },
		{ 41.713468, 43.838035, 47.091977, 50.020233, 53.548064, 58.041848, 59.813799, 61.435821, 64.439523, 67.334508, 70.315004, 73.599539, 75.461238, 77.570651, 83.307902, 88.250164, 92.688539, 98.028403, 101.775925,  },
		{ 42.493531, 44.639158, 47.924163, 50.879243, 54.438102, 58.969564, 60.755853, 62.390757, 65.417739, 68.334491, 71.336755, 74.644572, 76.519158, 78.642914, 84.417873, 89.391208, 93.856471, 99.227515, 102.996209,  },
		{ 43.275180, 45.441717, 48.757565, 51.739278, 55.328940, 59.897809, 61.698330, 63.346024, 66.396114, 69.334474, 72.358347, 75.689277, 77.576655, 79.714650, 85.527043, 90.531225, 95.023184, 100.425184, 104.214899,  },
		{ 44.058378, 46.245683, 49.592157, 52.600315, 56.220561, 60.826569, 62.641221, 64.301612, 67.374646, 70.334458, 73.379784, 76.733661, 78.633740, 80.785870, 86.635429, 91.670239, 96.188704, 101.621441, 105.432028,  },
		{ 44.843096, 47.051025, 50.427915, 53.462333, 57.112949, 61.755834, 63.584517, 65.257515, 68.353329, 71.334442, 74.401070, 77.777730, 79.690419, 81.856587, 87.743048, 92.808270, 97.353055, 102.816314, 106.647630,  },
		{ 45.629300, 47.857715, 51.264813, 54.325312, 58.006088, 62.685594, 64.528210, 66.213727, 69.332163, 72.334427, 75.422207, 78.821492, 80.746703, 82.926809, 88.849916, 93.945340, 98.516262, 104.009834, 107.861736,  },
		{ 46.416960, 48.665725, 52.102829, 55.189231, 58.899962, 63.615837, 65.472291, 67.170242, 70.311142, 73.334412, 76.443198, 79.864952, 81.802600, 83.996547, 89.956048, 95.081467, 99.678349, 105.202028, 109.074377,  },
		{ 47.206048, 49.475029, 52.941940, 56.054072, 59.794557, 64.546556, 66.416753, 68.127052, 71.290265, 74.334397, 77.464047, 80.908116, 82.858116, 85.065812, 91.061460, 96.216671, 100.839338, 106.392923, 110.285583,  },
		{ 47.996534, 50.285600, 53.782123, 56.919817, 60.689857, 65.477739, 67.361587, 69.084152, 72.269529, 75.334383, 78.484756, 81.950992, 83.913260, 86.134612, 92.166166, 97.350970, 101.999252, 107.582545, 111.495383,  },
		{ 48.788392, 51.097415, 54.623359, 57.786447, 61.585850, 66.409378, 68.306787, 70.041537, 73.248930, 76.334369, 79.505328, 82.993583, 84.968039, 87.202957, 93.270180, 98.484383, 103.158112, 108.770919, 112.703803,  },
		{ 49.581594, 51.910448, 55.465625, 58.653945, 62.482520, 67.341463, 69.252345, 70.999201, 74.228466, 77.334356, 80.525766, 84.035896, 86.022461, 88.270855, 94.373516, 99.616927, 104.315938, 109.958069, 113.910872,  },
		{ 50.376116, 52.724676, 56.308903, 59.522294, 63.379856, 68.273986, 70.198255, 71.957137, 75.208134, 78.334343, 81.546073, 85.077937, 87.076531, 89.338317, 95.476186, 100.748619, 105.472750, 111.144019, 115.116615,  },
		{ 51.171932, 53.540077, 57.153173, 60.391478, 64.277844, 69.206939, 71.144509, 72.915342, 76.187932, 79.334330, 82.566250, 86.119710, 88.130258, 90.405349, 96.578204, 101.879474, 106.628568, 112.328793, 116.321057,  },
		{ 51.969018, 54.356629, 57.998417, 61.261482, 65.176473, 70.140314, 72.091101, 73.873810, 77.167857, 80.334318, 83.586301, 87.161220, 89.183647, 91.471959, 97.679581, 103.009509, 107.783410, 113.512410, 117.524222,  },
		{ 52.767350, 55.174311, 58.844616, 62.132291, 66.075730, 71.074102, 73.038025, 74.832536, 78.147908, 81.334305, 84.606227, 88.202473, 90.236704, 92.538157, 98.780329, 104.138738, 108.937294, 114.694895, 118.726134,  },
		{ 53.566907, 55.993101, 59.691753, 63.003888, 66.975604, 72.008296, 73.985274, 75.791516, 79.128080, 82.334294, 85.626031, 89.243473, 91.289437, 93.603948, 99.880461, 105.267177, 110.090238, 115.876266, 119.926817,  },
		{ 54.367665, 56.812981, 60.539811, 63.876261, 67.876083, 72.942889, 74.932844, 76.750744, 80.108374, 83.334282, 86.645715, 90.284225, 92.341850, 94.669342, 100.979987, 106.394840, 111.242259, 117.056544, 121.126292,  },
		{ 55.169604, 57.633930, 61.388775, 64.749396, 68.777157, 73.877873, 75.880727, 77.710217, 81.088785, 84.334271, 87.665282, 91.324733, 93.393949, 95.734344, 102.078918, 107.521741, 112.393374, 118.235749, 122.324581,  },
		{ 55.972703, 58.455930, 62.238626, 65.623278, 69.678815, 74.813242, 76.828919, 78.669929, 82.069313, 85.334260, 88.684733, 92.365002, 94.445741, 96.798962, 103.177265, 108.647893, 113.543598, 119.413900, 123.521704,  },
		{ 56.776942, 59.278962, 63.089351, 66.497895, 70.581048, 75.748989, 77.777414, 79.629878, 83.049955, 86.334249, 89.704070, 93.405035, 95.497229, 97.863202, 104.275037, 109.773309, 114.692947, 120.591015, 124.717683,  },
		{ 57.582301, 60.103009, 63.940935, 67.373234, 71.483844, 76.685107, 78.726207, 80.590058, 84.030709, 87.334239, 90.723296, 94.444836, 96.548421, 98.927072, 105.372246, 110.898003, 115.841436, 121.767111, 125.912536,  },
		{ 58.388761, 60.928054, 64.793361, 68.249284, 72.387195, 77.621590, 79.675292, 81.550465, 85.011573, 88.334228, 91.742412, 95.484411, 97.599320, 99.990576, 106.468900, 112.021986, 116.989080, 122.942207, 127.106284,  },
		{ 59.196304, 61.754079, 65.646618, 69.126030, 73.291090, 78.558432, 80.624665, 82.511097, 85.992545, 89.334218, 92.761420, 96.523762, 98.649932, 101.053723, 107.565009, 113.145270, 118.135893, 124.116319, 128.298944,  },
		{ 60.004912, 62.581069, 66.500689, 70.003463, 74.195522, 79.495627, 81.574321, 83.471948, 86.973624, 90.334209, 93.780323, 97.562894, 99.700261, 102.116517, 108.660581, 114.267868, 119.281889, 125.289463, 129.490534,  },
		{ 60.814567, 63.409007, 67.355563, 70.881571, 75.100481, 80.433168, 82.524255, 84.433016, 87.954807, 91.334199, 94.799120, 98.601809, 100.750312, 103.178964, 109.755627, 115.389790, 120.427081, 126.461656, 130.681073,  },
		{ 61.625252, 64.237879, 68.211225, 71.760343, 76.005958, 81.371051, 83.474463, 85.394297, 88.936094, 92.334190, 95.817816, 99.640512, 101.800090, 104.241070, 110.850154, 116.511047, 121.571483, 127.632913, 131.870578,  },
		{ 62.436952, 65.067668, 69.067664, 72.639768, 76.911945, 82.309269, 84.424939, 86.355787, 89.917481, 93.334180, 96.836410, 100.679006, 102.849599, 105.302841, 111.944171, 117.631651, 122.715107, 128.803249, 133.059065,  },
		{ 63.249649, 65.898361, 69.924867, 73.519835, 77.818434, 83.247818, 85.375681, 87.317484, 90.898969, 94.334171, 97.854905, 101.717294, 103.898844, 106.364282, 113.037686, 118.751612, 123.857967, 129.972679, 134.246550,  },
		{ 64.063327, 66.729944, 70.782822, 74.400535, 78.725417, 84.186691, 86.326684, 88.279383, 91.880555, 95.334163, 98.873302, 102.755380, 104.947828, 107.425398, 114.130707, 119.870939, 125.000073, 131.141217, 135.433049,  },
		{ 64.877973, 67.562402, 71.641516, 75.281858, 79.632886, 85.125885, 87.277943, 89.241481, 92.862237, 96.334154, 99.891603, 103.793266, 105.996556, 108.486194, 115.223242, 120.989644, 126.141437, 132.308877, 136.618578,  },
		{ 65.693570, 68.395722, 72.500939, 76.163793, 80.540834, 86.065393, 88.229454, 90.203777, 93.844014, 97.334146, 100.909810, 104.830957, 107.045031, 109.546675, 116.315298, 122.107735, 127.282072, 133.475672, 137.803151,  },
		{ 66.510105, 69.229890, 73.361080, 77.046332, 81.449253, 87.005212, 89.181215, 91.166265, 94.825885, 98.334137, 101.927923, 105.868454, 108.093259, 110.606847, 117.406883, 123.225221, 128.421989, 134.641617, 138.986783,  },
		{ 67.327563, 70.064895, 74.221927, 77.929465, 82.358136, 87.945336, 90.133220, 92.128944, 95.807848, 99.334129, 102.945944, 106.905761, 109.141241, 111.666713, 118.498004, 124.342113, 129.561197, 135.806723, 140.169489,  },
	};

	private ChiSquared() {}

	/**
	 * Evaluate the cumulative distribution function of the {@link ChiSquared}
	 * distribution.
	 * @param n sample size such that there are {@code n-1} degrees of freedom. {@code 2 <= n}
	 * @param x value of random variable {@code X}
	 * @return {@code p = Pr( X <= x | n-1 )}
	 */
	public static double cdf( int n, double x ) {
		if ( n < 2 ) {
			throw new IllegalArgumentException();
		}
		// values converge after this point
		n = ( n >= xtable.length - 1 ) ? xtable.length - 1 : n;

		double r = 0D;
		boolean negative = x < 0;
		x = negative ? -x : x;
		double[] xrow = xtable[ n - 2 ];
		int i=0;
		for( double xx: xrow ) {
			if ( x >= xx ) {
				r = xp[ i ];
				i++;
			} else {
				break;
			}
		}
		r = negative ? ( 1 - r ) : r;
		return r;
	}
	/**
	 * Evaluate the inverse of the cumulative distribution function of the
	 * {@link ChiSquared} distribution.
	 * @param n sample size such that there are {@code n-1} degrees of freedom.{@code 2 <= n}
	 * @param p level of confidence. {@code 0 <= p <= 1}
	 * @return {@code x} &ni; {@code Pr( X <= x | n-1 ) = p}
	 */
	public static double inv( int n, double p ) {
		if ( p < 0 || p > 1 ) {
			throw new IllegalArgumentException();
		}
		if ( n < AStatistics.MIN_N_BEFORE_VALID_VARIANCE ) {
			throw new IllegalArgumentException();
		}
		// values converge after this point
		n = ( n >= xtable.length - 1 ) ? xtable.length - 1 : n;

		double r = 0;
		double pvalue = p;
		double[] xrow = xtable[ n - 2 ];
		int i;
		for( i=0; i < xp.length; i++ ) {
			double expeeeye = xp[ i ];
			if ( pvalue < expeeeye ) {
				i--;
				break;
			}
		}
		r = xrow[ i ];
		return r;
	}

	public static int minSamples( double q, double p ) {
		if ( p <= 0 || p >= 1 ) {
			throw new IllegalArgumentException();
		}
		if ( q <= 0 ) {
			throw new IllegalArgumentException();
		}
		int n;
		double p1, p2;
		double pp = 0;
		double one_plus_q_squared = Math.pow( (1 + q), 2 );
		double one_minus_q_squared = Math.pow( (1 - q), 2 );
		for( n = 2, p1 = 0, p2 = 0; pp < p; n++ ) {
			double dn = (double)n;
			double sqrt_n = Math.sqrt( dn );
			p1 = Normal.cdf( 0, q * sqrt_n );
			double p21 = ChiSquared.cdf( n, dn * one_plus_q_squared ) ;
			double p22 = ChiSquared.cdf( n, dn * one_minus_q_squared ) ;
			p2 = p21 - p22;
			pp = p1 * p2;
		}
		return n;
	}

	/**
	 * Perform a &Chi;&sup2; test to determine if the sampled values,
	 * {@code h}, are a good fit for the prototypical distribution,
	 * {@code proto}.
	 * The <a href="http://en.wikipedia.org/wiki/Null_hypothesis">null hypothesis</a>
	 * is that the distribution is not a good fit.
	 * @param prototype a reference distribution of the random variable {@code X}
	 * @param hypothesis data sampled from the random variable {@code X}
	 * @param p the confidence level the test must satisfy. 0 < p < 1
	 * @return true if the null hypothesis is invalid - i.e. if {@code prototype} is a good fit for {@code h}
	 */
	public static boolean test( double p, Histogram prototype, Histogram hypothesis ) {

		double bin_width = hypothesis.binWidth();
		double[] centers = hypothesis.binCenters();

		double[] hypothesis_data = hypothesis.data();
		double[] prototype_data = new double[ hypothesis_data.length ];
		for( int i = 0; i < hypothesis_data.length; i++ ) {
			double x1 = centers[ i ] - bin_width / 2;
			double x2 = centers[ i ] + bin_width / 2;
			prototype_data[ i ] = prototype.count( x1, x2 );
		}

		double sum = 0;
		for( int i = 0; i < hypothesis_data.length; sum += hypothesis_data[ i ], i++ );
		if ( 0 == sum ) {
			throw new NoSuchElementException();
		}
		for( int i = 0; i < hypothesis_data.length; hypothesis_data[ i ] /= sum, i++ );

		sum = 0;
		for( int i = 0; i < prototype_data.length; sum += prototype_data[ i ], i++ );
		if ( 0 == sum ) {
			throw new NoSuchElementException();
		}
		for( int i = 0; i < prototype_data.length; prototype_data[ i ] /= sum, i++ );

		int valid_samples = 0;
		sum = 0;
		for( int i = 0; i < prototype_data.length; i++ ) {
			if ( 0 == prototype_data[ i ] ) {
				// For prototype distributions that contain zero-values, Chi-Squared becomes unstable
				// because the prototype element, being in the denominator, forces the quotient positive infinity.
				// For zero values in the prototype distribution, the solution is one of following two options:
				// a) if the prototype is zero-valued and the observed is zero-valued, increment the sum by zero, counting the sample as valid (i.e. degrees of freedom is unchanged)
				// b) if the prototype is zero-valued and the observed is not zero-valued, increment the sum by zero, discounting the sample as valid (i.e. one fewer degrees of freedom)
				if ( 0 == hypothesis_data[ i ] ) {
					valid_samples++;
				}
				continue;
			}
			valid_samples++;
			double numerator = Math.pow( ( hypothesis_data[ i ] - prototype_data[ i ] ), 2 );
			double denominator = prototype_data[ i ];
			double quotient = numerator / denominator;
			sum += quotient;
		}

		double x;
		try {
			x = inv( valid_samples - 1, p );
		} catch( IllegalArgumentException e ) {
			return false;
		}
		return sum <= x;
	}
}
