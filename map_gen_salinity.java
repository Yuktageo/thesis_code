**
 * Function to mask clouds based on the pixel_qa band of Landsat SR data.
 * @param {ee.Image} image Input Landsat SR image
 * @return {ee.Image} Cloudmasked Landsat image
 */
// Load an image.
// var image = ee.Image('LANDSAT/LC08/C01/T1_SR/LC08_138045_20210118');
var image = ee.Image(surfaceref.filterDate('2020-01-01','2021-01-31').filterBounds(point).sort('CLOUD_COVER').first());
// print(image)
var trueColor432 = image.select(['B5', 'B4', 'B3']);
var trueColor432Vis = {};
Map.setCenter(88.30, 21.75, 9);
//Map.addLayer(trueColor432, trueColor432Vis, 'True Color (432)');
var rescale = image.divide(10000);
var rgbVis = {
  min: 0,
  max: 2000,
  bands: ['B4', 'B3', 'B2'],
};
var rgbVis2 = {
  min: 0,
  max: 0.2,
  bands: ['B4', 'B3', 'B2'],
};
Map.centerObject(rescale);
//Map.addLayer(image, rgbVis, 'Original');
Map.addLayer(rescale, rgbVis2, 'Rescaled');

// Step-2: Compute the SSC using an expression, R^2 vale=0.5666.
var bat = rescale.expression(
  '(B1*-1007.9780341451489+B2*1136.6787608261338+B3*-406.11910056202225+B4*-154.4703826501434+B5*-86.223818680173+51.732115069019365)',{
    'B1': rescale.select('B1'),
      'B2': rescale.select('B2'),
      'B3': rescale.select('B3'),
      'B4': rescale.select('B4'),
      'B5': rescale.select('B5')
      });
print(bat)
// Export.image.toDrive({
//   image: bat,
//   description: 'otpt',
//   fileFormat: 'GeoTIFF',
//   region: geometry,
//   scale:50
// });

//Map.addLayer(bat,{}, 'exp_Bath');
// var ssc= bat.exp();
// var ssc1=ssc.multiply(1000);
var ssc1=bat
print(ssc1)
//Map.addLayer(ssc1,{},'SSC');
var gsw = ee.Image('JRC/GSW1_0/GlobalSurfaceWater');
var occurrence = gsw.select('occurrence');
//Map.addLayer(occurrence, {}, 'occurrence');
var water_mask = occurrence.gt(90).mask(1);
//Map.addLayer(water_mask, {}, 'occurrence');
//var OC1 = rg.lt(1.3);
//var OC = OC1.updateMask(OC1);
//Map.addLayer(OC, {palette: ['0000FF']}, 'water');
var p=ssc1.updateMask(water_mask)
//Map.addLayer(p, {min: 1, max: 1000, palette: ['ffffff','FF2700','d600ff']}, 'Suspended sediment concentration');
//var LS1 = NR.gt(15);
//var LS = LS1.updateMask(LS1);
//Map.addLayer(LS, {palette: ['f7e084']}, 'Land');
// create vizualization parameters
var viz = {min:0.1, max:35, palette:['0000FF','21f600','FDFF98','FF2100', 'd600ff']};
// add the map
Map.addLayer(p, viz,'Suspended sediment concentration1');

// set position of panel
var legend = ui.Panel({
style: {
position: 'bottom-right',
padding: '8px 15px'
}
});
 
// Create legend title
var legendTitle = ui.Label({
value: 'salinity (psu)',
style: {
fontWeight: 'bold',
fontSize: '18px',
margin: '0 0 4px 0',
padding: '0'
}
});
 
// Add the title to the panel
legend.add(legendTitle);
 
// create the legend image
var lon = ee.Image.pixelLonLat().select('latitude');
var gradient = lon.multiply((viz.max-viz.min)/100.0).add(viz.min);
var legendImage = gradient.visualize(viz);
 
// create text on top of legend
var panel = ui.Panel({
widgets: [
ui.Label(viz['max'])
],
});
 
legend.add(panel);
 
// create thumbnail from the image
var thumbnail = ui.Thumbnail({
image: legendImage,
params: {bbox:'0,0,10,100', dimensions:'10x200'},
style: {padding: '1px', position: 'bottom-center'}
});
 
// add the thumbnail to the legend
legend.add(thumbnail);
 
// create text on top of legend
var panel = ui.Panel({
widgets: [
ui.Label(viz['min'])
],
});
 
legend.add(panel);
 
Map.add(legend);
