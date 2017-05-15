import React, { Component } from 'react';
import { 
  View, 
  Text, 
  Image 
} from 'react-native';

class Widget extends Component {
  render() {
    return (
      <View>
        <Text>Widget!</Text>
        <Image source={require('./image/liking.png')}/>
      </View>
    );
  }
}

export default Widget;
