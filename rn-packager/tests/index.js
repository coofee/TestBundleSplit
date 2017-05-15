import React, { Component } from 'react';
import { AppRegistry, Text, View, Image } from 'react-native';
import ImagePicker from 'react-native-image-picker';
import Widget from './widget';

class HelloWorldApp extends Component {
  render() {
    return (
      <View>
        <Text>Hello world!</Text>
        <Image source={require('./image/liking.png')}/>
        <Widget/>
      </View>
    );
  }
}

AppRegistry.registerComponent('HelloWorldApp', () => HelloWorldApp);
